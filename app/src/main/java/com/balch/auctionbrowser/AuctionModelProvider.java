/*
 * Author: Balch
 * Created: 9/6/14 10:10 AM
 *
 * This file is part of MockTrade.
 *
 * MockTrade is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MockTrade is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MockTrade.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright (C) 2014
 */

package com.balch.auctionbrowser;

import com.balch.android.app.framework.ModelProvider;
import com.balch.android.app.framework.sql.SqlConnection;

public interface AuctionModelProvider extends ModelProvider {

    SqlConnection getSqlConnection();

    ModelApiFactory getModelApiFactory();

}
