package com.simplemobiletools.musicplayer.new_architecture.feature_tracks

import android.app.Activity
import android.app.SearchManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuItemCompat
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.simplemobiletools.musicplayer.new_architecture.shared.ui.dialogs.PermissionRequiredDialog
import com.simplemobiletools.musicplayer.new_architecture.shared.helpers.NavigationIcon
import com.simplemobiletools.musicplayer.new_architecture.shared.helpers.ensureBackgroundThread
import com.simplemobiletools.musicplayer.new_architecture.shared.helpers.isOreoPlus
import com.simplemobiletools.musicplayer.new_architecture.shared.helpers.isQPlus
import com.simplemobiletools.musicplayer.R
import com.simplemobiletools.musicplayer.new_architecture.shared.SimpleMusicActivity
import com.simplemobiletools.musicplayer.new_architecture.feature_tracks.TracksAdapter.Companion.TYPE_ALBUM
import com.simplemobiletools.musicplayer.new_architecture.feature_tracks.TracksAdapter.Companion.TYPE_FOLDER
import com.simplemobiletools.musicplayer.new_architecture.feature_tracks.TracksAdapter.Companion.TYPE_PLAYLIST
import com.simplemobiletools.musicplayer.new_architecture.feature_tracks.TracksAdapter.Companion.TYPE_TRACKS
import com.simplemobiletools.musicplayer.databinding.ActivityTracksBinding
import com.simplemobiletools.musicplayer.new_architecture.shared.ui.dialogs.ChangeSortingDialog
import com.simplemobiletools.musicplayer.new_architecture.shared.ui.dialogs.ExportPlaylistDialog
import com.simplemobiletools.musicplayer.new_architecture.shared.ui.dialogs.filepicker.FilePickerDialog
import com.simplemobiletools.musicplayer.new_architecture.shared.extensions.areSystemAnimationsEnabled
import com.simplemobiletools.musicplayer.new_architecture.shared.extensions.audioHelper
import com.simplemobiletools.musicplayer.new_architecture.shared.extensions.beGone
import com.simplemobiletools.musicplayer.new_architecture.shared.extensions.beGoneIf
import com.simplemobiletools.musicplayer.new_architecture.shared.extensions.beVisibleIf
import com.simplemobiletools.musicplayer.new_architecture.shared.extensions.config
import com.simplemobiletools.musicplayer.new_architecture.shared.extensions.getFolderTracks
import com.simplemobiletools.musicplayer.new_architecture.shared.extensions.getMediaStoreIdFromPath
import com.simplemobiletools.musicplayer.new_architecture.shared.extensions.getProperPrimaryColor
import com.simplemobiletools.musicplayer.new_architecture.shared.extensions.getProperTextColor
import com.simplemobiletools.musicplayer.new_architecture.shared.extensions.isAudioFast
import com.simplemobiletools.musicplayer.new_architecture.shared.extensions.openNotificationSettings
import com.simplemobiletools.musicplayer.new_architecture.shared.extensions.rescanPaths
import com.simplemobiletools.musicplayer.new_architecture.shared.extensions.showErrorToast
import com.simplemobiletools.musicplayer.new_architecture.shared.extensions.toFileDirItem
import com.simplemobiletools.musicplayer.new_architecture.shared.extensions.toast
import com.simplemobiletools.musicplayer.new_architecture.shared.extensions.underlineText
import com.simplemobiletools.musicplayer.new_architecture.shared.extensions.viewBinding
import com.simplemobiletools.musicplayer.new_architecture.shared.helpers.ACTIVITY_PLAYLIST_FOLDER
import com.simplemobiletools.musicplayer.new_architecture.shared.helpers.ALBUM
import com.simplemobiletools.musicplayer.new_architecture.shared.helpers.FOLDER
import com.simplemobiletools.musicplayer.new_architecture.shared.helpers.GENRE
import com.simplemobiletools.musicplayer.new_architecture.shared.helpers.M3uExporter
import com.simplemobiletools.musicplayer.new_architecture.shared.helpers.M3uExporter.ExportResult
import com.simplemobiletools.musicplayer.new_architecture.shared.helpers.MIME_TYPE_M3U
import com.simplemobiletools.musicplayer.new_architecture.shared.helpers.PLAYLIST
import com.simplemobiletools.musicplayer.new_architecture.shared.helpers.RoomHelper
import com.simplemobiletools.musicplayer.new_architecture.shared.helpers.getPermissionToRequest
import com.simplemobiletools.musicplayer.models.Album
import com.simplemobiletools.musicplayer.models.AlbumHeader
import com.simplemobiletools.musicplayer.models.Events
import com.simplemobiletools.musicplayer.models.Genre
import com.simplemobiletools.musicplayer.models.ListItem
import com.simplemobiletools.musicplayer.models.Playlist
import com.simplemobiletools.musicplayer.models.Track
import com.simplemobiletools.musicplayer.models.sortSafely
import java.io.OutputStream
import org.greenrobot.eventbus.EventBus

// this activity is used for displaying Playlist and Folder tracks, also Album tracks with a possible album header at the top
// Artists -> Albums -> Tracks
class TracksActivity : SimpleMusicActivity() {
    private val PICK_EXPORT_FILE_INTENT = 2

    private var isSearchOpen = false
    private var searchMenuItem: MenuItem? = null
    private var tracksIgnoringSearch = ArrayList<Track>()
    private var playlist: Playlist? = null
    private var folder: String? = null
    private var sourceType = 0
    private var lastFilePickerPath = ""

    private val binding by viewBinding(ActivityTracksBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        isMaterialActivity = true
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setupOptionsMenu()
        refreshMenuItems()

        updateMaterialActivityViews(
            binding.tracksCoordinator,
            binding.tracksHolder,
            useTransparentNavigation = true,
            useTopSearchMenu = false
        )
        setupMaterialScrollListener(binding.tracksList, binding.tracksToolbar)

        val properPrimaryColor = getProperPrimaryColor()
        binding.tracksFastscroller.updateColors(properPrimaryColor)
        binding.tracksPlaceholder.setTextColor(getProperTextColor())
        binding.tracksPlaceholder2.setTextColor(properPrimaryColor)
        binding.tracksPlaceholder2.underlineText()
        binding.tracksPlaceholder2.setOnClickListener {
            addFolderToPlaylist()
        }

        setupCurrentTrackBar(binding.currentTrackBar.root)
    }

    override fun onResume() {
        super.onResume()
        setupToolbar(binding.tracksToolbar, NavigationIcon.Arrow, searchMenuItem = searchMenuItem)
        refreshTracks()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultData)
        if (requestCode == PICK_EXPORT_FILE_INTENT && resultCode == Activity.RESULT_OK && resultData != null && resultData.data != null) {
            try {
                val outputStream = contentResolver.openOutputStream(resultData.data!!)
                exportPlaylistTo(outputStream)
            } catch (e: Exception) {
                showErrorToast(e)
            }
        }
    }

    private fun refreshMenuItems() {
        binding.tracksToolbar.menu.apply {
            findItem(R.id.search).isVisible = sourceType != TYPE_ALBUM
            findItem(R.id.sort).isVisible = sourceType != TYPE_ALBUM
            findItem(R.id.add_file_to_playlist).isVisible = sourceType == TYPE_PLAYLIST
            findItem(R.id.add_folder_to_playlist).isVisible = sourceType == TYPE_PLAYLIST
            findItem(R.id.export_playlist).isVisible = sourceType == TYPE_PLAYLIST && isOreoPlus()
        }
    }

    private fun setupOptionsMenu() {
        setupSearch(binding.tracksToolbar.menu)
        binding.tracksToolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.sort -> showSortingDialog()
                R.id.add_file_to_playlist -> addFileToPlaylist()
                R.id.add_folder_to_playlist -> addFolderToPlaylist()
                R.id.export_playlist -> tryExportPlaylist()
                else -> return@setOnMenuItemClickListener false
            }
            return@setOnMenuItemClickListener true
        }
    }

    private fun setupSearch(menu: Menu) {
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        searchMenuItem = menu.findItem(R.id.search)
        (searchMenuItem!!.actionView as SearchView).apply {
            setSearchableInfo(searchManager.getSearchableInfo(componentName))
            isSubmitButtonEnabled = false
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String) = false

                override fun onQueryTextChange(newText: String): Boolean {
                    if (isSearchOpen) {
                        onSearchQueryChanged(newText)
                    }
                    return true
                }
            })
        }

        MenuItemCompat.setOnActionExpandListener(
            searchMenuItem,
            object : MenuItemCompat.OnActionExpandListener {
                override fun onMenuItemActionExpand(item: MenuItem?): Boolean {
                    onSearchOpened()
                    isSearchOpen = true
                    return true
                }

                override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {
                    onSearchClosed()
                    isSearchOpen = false
                    return true
                }
            })
    }

    private fun refreshTracks() {
        val playlistType = object : TypeToken<Playlist>() {}.type
        playlist = Gson().fromJson<Playlist>(intent.getStringExtra(PLAYLIST), playlistType)
        if (playlist != null) {
            sourceType = TYPE_PLAYLIST
        }

        val albumType = object : TypeToken<Album>() {}.type
        val album = Gson().fromJson<Album>(intent.getStringExtra(ALBUM), albumType)
        if (album != null) {
            sourceType = TYPE_ALBUM
        }

        val genreType = object : TypeToken<Genre>() {}.type
        val genre = Gson().fromJson<Genre>(intent.getStringExtra(GENRE), genreType)
        if (genre != null) {
            sourceType = TYPE_TRACKS
        }

        folder = intent.getStringExtra(FOLDER)
        if (folder != null) {
            sourceType = TYPE_FOLDER
            binding.tracksPlaceholder2.beGone()
        }

        val titleToUse = playlist?.title ?: album?.title ?: genre?.title ?: folder ?: ""
        binding.tracksToolbar.title = titleToUse
        refreshMenuItems()

        ensureBackgroundThread {
            val tracks = ArrayList<Track>()
            val listItems = ArrayList<ListItem>()
            when (sourceType) {
                TYPE_PLAYLIST -> {
                    val playlistTracks = audioHelper.getPlaylistTracks(playlist!!.id)
                    runOnUiThread {
                        binding.tracksPlaceholder.beVisibleIf(playlistTracks.isEmpty())
                        binding.tracksPlaceholder2.beVisibleIf(playlistTracks.isEmpty())
                    }

                    tracks.addAll(playlistTracks)
                    listItems.addAll(tracks)
                }

                TYPE_ALBUM -> {
                    val albumTracks = audioHelper.getAlbumTracks(album.id)
                    tracks.addAll(albumTracks)

                    val header = AlbumHeader(
                        album.id,
                        album.title,
                        album.coverArt,
                        album.year,
                        tracks.size,
                        tracks.sumOf { it.duration },
                        album.artist
                    )
                    listItems.add(header)
                    listItems.addAll(tracks)
                }

                TYPE_TRACKS -> {
                    val genreTracks = audioHelper.getGenreTracks(genre.id)
                    tracks.addAll(genreTracks)
                }

                else -> {
                    val folderTracks = audioHelper.getFolderTracks(folder.orEmpty())
                    runOnUiThread {
                        binding.tracksPlaceholder.beVisibleIf(folderTracks.isEmpty())
                    }

                    tracks.addAll(folderTracks)
                    listItems.addAll(tracks)
                }
            }

            runOnUiThread {
                if (sourceType == TYPE_ALBUM) {
                    val currAdapter = binding.tracksList.adapter
                    if (currAdapter == null) {
                        TracksHeaderAdapter(this, listItems, binding.tracksList) {
                            itemClicked(it as Track)
                        }.apply {
                            binding.tracksList.adapter = this
                        }

                        if (areSystemAnimationsEnabled) {
                            binding.tracksList.scheduleLayoutAnimation()
                        }
                    } else {
                        (currAdapter as TracksHeaderAdapter).updateItems(listItems)
                    }
                } else {
                    val currAdapter = binding.tracksList.adapter
                    if (currAdapter == null) {
                        TracksAdapter(
                            activity = this,
                            recyclerView = binding.tracksList,
                            sourceType = sourceType,
                            folder = folder,
                            playlist = playlist,
                            items = tracks
                        ) {
                            itemClicked(it as Track)
                        }.apply {
                            binding.tracksList.adapter = this
                        }

                        if (areSystemAnimationsEnabled) {
                            binding.tracksList.scheduleLayoutAnimation()
                        }
                    } else {
                        (currAdapter as TracksAdapter).updateItems(tracks)
                    }
                }
            }
        }
    }

    private fun getTracksAdapter() = binding.tracksList.adapter as? TracksAdapter

    private fun showSortingDialog() {
        ChangeSortingDialog(this, ACTIVITY_PLAYLIST_FOLDER, playlist, folder) {
            val adapter = getTracksAdapter() ?: return@ChangeSortingDialog
            val tracks = adapter.items
            val sorting = when (sourceType) {
                TYPE_PLAYLIST -> config.getProperPlaylistSorting(playlist?.id ?: -1)
                TYPE_TRACKS -> config.trackSorting
                else -> config.getProperFolderSorting(folder ?: "")
            }

            tracks.sortSafely(sorting)
            adapter.updateItems(tracks, forceUpdate = true)

            if (sourceType == TYPE_TRACKS) {
                EventBus.getDefault().post(Events.RefreshTracks())
            }
        }
    }

    private fun addFileToPlaylist() {
        FilePickerDialog(this, lastFilePickerPath, enforceStorageRestrictions = false) { path ->
            ensureBackgroundThread {
                lastFilePickerPath = path
                if (path.isAudioFast()) {
                    addTrackFromPath(path, true)
                } else {
                    toast(R.string.invalid_file_format)
                }
            }
        }
    }

    private fun addTrackFromPath(path: String, rescanWrongPath: Boolean) {
        val mediaStoreId = getMediaStoreIdFromPath(path)
        if (mediaStoreId == 0L) {
            if (rescanWrongPath) {
                rescanPaths(arrayListOf(path)) {
                    addTrackFromPath(path, false)
                }
            } else {
                toast(R.string.unknown_error_occurred)
            }
        } else {
            var track = audioHelper.getTrack(mediaStoreId)
            if (track == null) {
                track = RoomHelper(this).getTrackFromPath(path)
            }

            if (track != null) {
                track.id = 0
                track.playListId = playlist!!.id
                audioHelper.insertTracks(listOf(track))
                refreshPlaylist()
            }
        }
    }

    private fun addFolderToPlaylist() {
        FilePickerDialog(this, pickFile = false, enforceStorageRestrictions = false) {
            ensureBackgroundThread {
                getFolderTracks(it, true) { tracks ->
                    tracks.forEach {
                        it.playListId = playlist!!.id
                    }

                    audioHelper.insertTracks(tracks)
                    refreshPlaylist()
                }
            }
        }
    }

    private fun onSearchOpened() {
        tracksIgnoringSearch = getTracksAdapter()?.items ?: return
    }

    private fun onSearchClosed() {
        getTracksAdapter()?.updateItems(tracksIgnoringSearch)
        binding.tracksPlaceholder.beGoneIf(tracksIgnoringSearch.isNotEmpty())
    }

    private fun onSearchQueryChanged(text: String) {
        val filtered = tracksIgnoringSearch.filter {
            it.title.contains(text, true) || ("${it.artist} - ${it.album}").contains(text, true)
        }.toMutableList() as ArrayList<Track>
        getTracksAdapter()?.updateItems(filtered, text)
        binding.tracksPlaceholder.beGoneIf(filtered.isNotEmpty())
    }

    private fun refreshPlaylist() {
        EventBus.getDefault().post(Events.PlaylistsUpdated())

        val newTracks = audioHelper.getPlaylistTracks(playlist!!.id)
        runOnUiThread {
            getTracksAdapter()?.updateItems(newTracks)
            binding.tracksPlaceholder.beVisibleIf(newTracks.isEmpty())
            binding.tracksPlaceholder2.beVisibleIf(newTracks.isEmpty())
        }
    }

    private fun itemClicked(track: Track) {
        val tracks = when (sourceType) {
            TYPE_ALBUM -> (binding.tracksList.adapter as? TracksHeaderAdapter)?.items?.filterIsInstance<Track>()
            else -> getTracksAdapter()?.items
        } ?: ArrayList()

        handleNotificationPermission { granted ->
            if (granted) {
                val startIndex = tracks.indexOf(track)
                prepareAndPlay(tracks, startIndex)
            } else {
                PermissionRequiredDialog(
                    this,
                    R.string.allow_notifications_music_player,
                    { openNotificationSettings() })
            }
        }
    }

    private fun tryExportPlaylist() {
        if (isQPlus()) {
            ExportPlaylistDialog(this, config.lastExportPath, true) { file ->
                Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                    type = MIME_TYPE_M3U
                    putExtra(Intent.EXTRA_TITLE, file.name)
                    addCategory(Intent.CATEGORY_OPENABLE)

                    try {
                        startActivityForResult(this, PICK_EXPORT_FILE_INTENT)
                    } catch (e: ActivityNotFoundException) {
                        toast(R.string.system_service_disabled, Toast.LENGTH_LONG)
                    } catch (e: Exception) {
                        showErrorToast(e)
                    }
                }
            }
        } else {
            handlePermission(getPermissionToRequest()) { granted ->
                if (granted) {
                    ExportPlaylistDialog(this, config.lastExportPath, false) { file ->
                        getFileOutputStream(file.toFileDirItem(this), true) { outputStream ->
                            exportPlaylistTo(outputStream)
                        }
                    }
                }
            }
        }
    }

    private fun exportPlaylistTo(outputStream: OutputStream?) {
        val tracks = getTracksAdapter()?.items
        if (tracks.isNullOrEmpty()) {
            toast(R.string.no_entries_for_exporting)
            return
        }

        M3uExporter(this).exportPlaylist(outputStream, tracks) { result ->
            toast(
                when (result) {
                    ExportResult.EXPORT_OK -> R.string.exporting_successful
                    ExportResult.EXPORT_PARTIAL -> R.string.exporting_some_entries_failed
                    else -> R.string.exporting_failed
                }
            )
        }
    }
}
