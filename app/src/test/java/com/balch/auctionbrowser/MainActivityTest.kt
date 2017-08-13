package com.balch.auctionbrowser

import android.os.Bundle
import com.balch.auctionbrowser.test.BaseTest
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations.initMocks

class MainActivityTest: BaseTest() {

    @Mock lateinit private var presenter: AuctionPresenter

    lateinit private var activity: MainActivity

    @Before
    fun setUp() {
        initMocks(this)

        activity = spy(MainActivity())

        activity.presenter = presenter

        doReturn(false).`when`(activity).handleIntent()
    }

    @Test
    fun testOnCreateInternal() {

        val bundle = Bundle()

        //region Execute Test
        activity.onCreateInternal(bundle)
        //endregion

        verify(presenter).initialize(eq(bundle))
        verify(activity).handleIntent()
    }

}