# EBay Auction Browser

### Architecture Highlights
* 100% **Kotlin**
* Uses **ViewModel** and **LiveData** from [Android Architecture Components](https://developer.android.com/topic/libraries/architecture/index.html)
* Uses **Retrofit2**/**RxJava2**/**Gson** to query [EBay API](http://developer.ebay.com/Devzone/finding/CallRef/findItemsByKeywords.html)
* **RecyclerView** with endless scrolling pattern
* Local **SQLite** database for user attached note
* Gradle Dependency on [AppFramework Library](https://github.com/balch/MockTrade#application-framework)
    * **MVP**
    * [Source](https://github.com/balch/MockTrade/tree/master/AppFramework)

### Getting Started
* Requires **Android Studio 3.0**
* **EBay** API Key
    * [EBay Quick Start Guide](https://go.developer.ebay.com/quick-start-guide)
    * set `ebay_app_id` resource string in  _app/build.gradle_
        ```
        resValue "string", "ebay_app_id", "your ID here"
        ```

![Screen Shot 1](./AuctionBrowser_ss1.png)

![Screen Shot 3](./AuctionBrowser_ss3.png)
