/*
 * Author: Balch
 *
 * This file is part of AuctionBrowser.
 *
 * AuctionBrowser is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * AuctionBrowser is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MockTrade.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright (C) 2017
 *
 */

package com.balch.auctionbrowser

import android.content.Context
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleOwner
import com.balch.auctionbrowser.auction.AuctionAdapter
import com.balch.auctionbrowser.auction.AuctionPresenter
import com.balch.auctionbrowser.auction.AuctionView
import com.balch.auctionbrowser.auction.AuctionViewModel
import com.balch.auctionbrowser.auction.model.Auction
import com.balch.auctionbrowser.note.Note
import com.balch.auctionbrowser.note.NoteDao
import com.balch.auctionbrowser.note.NotesRepository
import com.balch.auctionbrowser.test.BaseTest
import com.balch.auctionbrowser.test.anyArg
import com.balch.auctionbrowser.test.makeCaptor
import com.balch.auctionbrowser.test.uninitialized
import com.uber.autodispose.lifecycle.TestLifecycleScopeProvider
import io.reactivex.Single
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations.initMocks
import java.util.concurrent.Executor
import kotlin.test.assertNull


class AuctionPresenterTest : BaseTest() {

    @Mock
    private lateinit var mockView: AuctionView
    @Mock
    private lateinit var fragmentManager: FragmentManager
    @Mock
    private lateinit var lifecycleOwner: LifecycleOwner
    @Mock
    private lateinit var context: Context
    @Mock
    private lateinit var executor: Executor
    @Mock
    private lateinit var noteDao: NoteDao

    lateinit var notesRepository: NotesRepository
    lateinit var auctionAdapter: AuctionAdapter
    lateinit var auctionViewModel: AuctionViewModel

    private lateinit var presenter: AuctionPresenter

    @Before
    fun setUp() {
        initMocks(this)

        notesRepository = spy(NotesRepository(noteDao))
        auctionAdapter = spy(AuctionAdapter())
        auctionViewModel = spy(AuctionViewModel(context, executor))
        presenter = spy(AuctionPresenter(mockView, auctionViewModel, notesRepository, fragmentManager,
                lifecycleOwner, auctionAdapter,
                TestLifecycleScopeProvider.createInitial(TestLifecycleScopeProvider.TestLifecycle.STARTED)))

        doNothing().`when`(auctionAdapter).notifyDataSetChanged()
        doReturn(Single.just(listOf(1))).`when`(noteDao).insert(anyArg())
        doReturn(Single.just(1)).`when`(noteDao).update(anyArg())
        doReturn(Single.just(1)).`when`(noteDao).delete(anyArg())
    }

    @Test
    fun testSaveNote_update() {
        val auction = mock(Auction::class.java)
        val note = mock(Note::class.java)
        val text = "test text"

        //region Execute Test
        presenter.saveNote(auction, note, text)
        testScheduler.triggerActions()
        //endregion

        verify(note).noteText = text
        verify(notesRepository).update(note)
    }

    @Test
    fun testSaveNote_insert() {
        val auction = mock(Auction::class.java)
        val text = "test text"

        //region Execute Test
        presenter.saveNote(auction, null, text)
        testScheduler.triggerActions()
        //endregion

        val (verifier, captors) = makeCaptor(notesRepository, Note::class.java)
        verifier.insert(uninitialized())

        val note: Note = captors[0].value as Note
        assertThat(note.noteText).isEqualTo(text)

        verify(auction).note = note
    }

    @Test
    fun testClearNote() {
        val auction: Auction = mock(Auction::class.java)
        val note: Note = mock(Note::class.java)

        //region Execute Test
        presenter.clearNote(auction, note)
        testScheduler.triggerActions()
        //endregion

        verify(notesRepository).delete(note)

        assertNull(auction.note)
    }

    @Test
    fun testClearNote_null() {
        val auction: Auction = mock(Auction::class.java)

        //region Execute Test
        presenter.clearNote(auction, null)
        //endregion

        verify(notesRepository, never()).delete(anyArg())
    }
}
