/*
 *  Author: Balch
 *  Created: 6/14/15 11:21 AM
 *
 *  This file is part of BB_Challenge.
 *
 *  BB_Challenge is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  BB_Challenge is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with MockTrade.  If not, see <http://www.gnu.org/licenses/>.
 *
 *  Copyright (C) 2015
 *
 */

package com.balch.auctionbrowser;

import android.os.Bundle;

import com.balch.android.app.framework.BaseAppCompatActivity;
import com.balch.android.app.framework.BasePresenter;
import com.balch.auctionbrowser.auction.AuctionPresenter;
import com.balch.auctionbrowser.auction.AuctionView;
import com.balch.auctionbrowser.auction.EBayModel;
import com.balch.auctionbrowser.note.NotesModel;


public class MainActivity extends BaseAppCompatActivity<AuctionView> {

    @Override
    protected void initialize(Bundle bundle) {

    }

    @Override
    protected BasePresenter createPresenter(AuctionView auctionView) {
        ModelProvider modelProvider = (ModelProvider)getApplication();
        return new AuctionPresenter(auctionView,
                new EBayModel(getString(R.string.ebay_app_id_production), modelProvider),
                new NotesModel(modelProvider));
    }

    @Override
    protected AuctionView createView() {
        return new AuctionView(this);
    }
}
