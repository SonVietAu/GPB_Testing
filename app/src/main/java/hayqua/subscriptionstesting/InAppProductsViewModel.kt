package hayqua.subscriptionstesting

import android.app.Activity
import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.android.billingclient.api.*

class InAppProductsViewModel(application: Application) : AndroidViewModel(application) {

    lateinit var billingClient: BillingClient

    // ensure that the billingClient is connected before doing any billing action
    var isServiceConnectedMLD = MutableLiveData<Boolean>()

    val inAppsPurchasedMLD = MutableLiveData<Purchase.PurchasesResult>()
    private val allInAppsSkuDetailsMLD = MutableLiveData<List<SkuDetails>>()
    val availableInAppsSkuDetailsMLD = MutableLiveData<List<SkuDetails>>()

    val subscribedDetailsMLD = MutableLiveData<Purchase.PurchasesResult>()
    private val allSubscriptionsSkuDetailsMLD = MutableLiveData<List<SkuDetails>>()
    val availableSubscriptionsSkuDetailsMLD = MutableLiveData<List<SkuDetails>>()

    val messageMLD = MutableLiveData<String>()

    init {
        createAndStartBillingClient()
    }

    private fun createAndStartBillingClient() {
        billingClient = BillingClient.newBuilder(getApplication()).setListener(object : PurchasesUpdatedListener {
            override fun onPurchasesUpdated(@BillingClient.BillingResponse responseCode: Int, purchases: MutableList<Purchase>?) {

                // Reset purchased and available
                queryINAPPAsync()
                querySUBSAsync()

                messageMLD.value = "Purchases with orderId ${
                if (purchases != null && purchases.isNotEmpty()) {
                    val purchasesSB = java.lang.StringBuffer("(")
                    purchases.forEachIndexed { index, purchase ->
                        if (index != purchases.size - 1)
                            purchasesSB.append("${purchase.orderId}, ")
                        else
                            purchasesSB.append(purchase.orderId)
                    }
                    purchasesSB.append(")")
                    purchasesSB.toString()
                } else {
                    "(a null or empty list)"
                }
                } updated with responseCode ${
                when (responseCode) {
                    BillingClient.BillingResponse.BILLING_UNAVAILABLE -> "BILLING_UNAVAILABLE"
                    BillingClient.BillingResponse.USER_CANCELED -> "USER_CANCELED"
                    BillingClient.BillingResponse.SERVICE_UNAVAILABLE -> "SERVICE_UNAVAILABLE"
                    BillingClient.BillingResponse.SERVICE_DISCONNECTED -> "SERVICE_DISCONNECTED"
                    BillingClient.BillingResponse.ITEM_UNAVAILABLE -> "ITEM_UNAVAILABLE"
                    BillingClient.BillingResponse.ITEM_NOT_OWNED -> "ITEM_NOT_OWNED"
                    BillingClient.BillingResponse.ITEM_ALREADY_OWNED -> "ITEM_ALREADY_OWNED"
                    BillingClient.BillingResponse.ERROR -> "ERROR"
                    BillingClient.BillingResponse.DEVELOPER_ERROR -> "DEVELOPER_ERROR"
                    BillingClient.BillingResponse.FEATURE_NOT_SUPPORTED -> "FEATURE_NOT_SUPPORTED"
                    BillingClient.BillingResponse.OK -> "OK"
                    else -> "$responseCode"
                }}"

                Log.d("TempGroupIcon", messageMLD.value)

            }
        }).build()
        startServiceConnection()
    }

    private val inAppSkuDetailsResponseListener = object : SkuDetailsResponseListener {
        override fun onSkuDetailsResponse(responseCode: Int, skuDetailsList: MutableList<SkuDetails>?) {
            if (responseCode != BillingClient.BillingResponse.OK) {
                messageMLD.value = "InApp SkuDetails request responded with responseCode ${
                when (responseCode) {
                    BillingClient.BillingResponse.BILLING_UNAVAILABLE -> "BILLING_UNAVAILABLE"
                    BillingClient.BillingResponse.USER_CANCELED -> "USER_CANCELED"
                    BillingClient.BillingResponse.SERVICE_UNAVAILABLE -> "SERVICE_UNAVAILABLE"
                    BillingClient.BillingResponse.SERVICE_DISCONNECTED -> "SERVICE_DISCONNECTED"
                    BillingClient.BillingResponse.ITEM_UNAVAILABLE -> "ITEM_UNAVAILABLE"
                    BillingClient.BillingResponse.ITEM_NOT_OWNED -> "ITEM_NOT_OWNED"
                    BillingClient.BillingResponse.ITEM_ALREADY_OWNED -> "ITEM_ALREADY_OWNED"
                    BillingClient.BillingResponse.ERROR -> "ERROR"
                    BillingClient.BillingResponse.DEVELOPER_ERROR -> "DEVELOPER_ERROR"
                    BillingClient.BillingResponse.FEATURE_NOT_SUPPORTED -> "FEATURE_NOT_SUPPORTED"
                    BillingClient.BillingResponse.OK -> {
                        null/*Already Handled*/
                    }
                    else -> "$responseCode"
                }
                }"
            } else {
                allInAppsSkuDetailsMLD.value = skuDetailsList

                // Determine purchased
                inAppsPurchasedMLD.value = billingClient.queryPurchases(BillingClient.SkuType.INAPP)

                val purchasedList = inAppsPurchasedMLD.value
                if (purchasedList != null && skuDetailsList != null) {
                    availableInAppsSkuDetailsMLD.value = skuDetailsList.filter { skuDetails ->
                        val purchased = purchasedList.purchasesList.firstOrNull { element ->
                            element.sku == skuDetails.sku
                        }
                        purchased == null
                    }
                } else {
                    availableSubscriptionsSkuDetailsMLD.value = null
                }
            }

        }
    }

    fun queryINAPPAsync() {

        // Needs two queries, one for INAPP and the other for SUBS. Seem there is no way to query both at the same time.
        // Creating a runnable from the request to use it inside our connection retry policy below
        val queryRequest = Runnable {

            val itemType = BillingClient.SkuType.INAPP
            val skuList = listOf(
                "android.test.purchased",
                "android.test.canceled",
                "android.test.item_unavailable",
                "android.test.random_sku_sdjf",
                "in_app_10",
                "in_app_20"
            )

            // Query the purchase async
            val params = SkuDetailsParams.newBuilder()
            params.setSkusList(skuList).setType(itemType)
            billingClient.querySkuDetailsAsync(params.build(), inAppSkuDetailsResponseListener)
            Log.d("TempGroupIcon", "Running inApp query")
        }

        executeServiceRequest(queryRequest)
    }

    private val subscriptionsSkuDetailsResponseListener = object : SkuDetailsResponseListener {
        override fun onSkuDetailsResponse(responseCode: Int, skuDetailsList: MutableList<SkuDetails>?) {
            if (responseCode != BillingClient.BillingResponse.OK) {
                messageMLD.value = "SUBS SkuDetails request responded with error responseCode ${
                when (responseCode) {
                    BillingClient.BillingResponse.BILLING_UNAVAILABLE -> "BILLING_UNAVAILABLE"
                    BillingClient.BillingResponse.USER_CANCELED -> "USER_CANCELED"
                    BillingClient.BillingResponse.SERVICE_UNAVAILABLE -> "SERVICE_UNAVAILABLE"
                    BillingClient.BillingResponse.SERVICE_DISCONNECTED -> "SERVICE_DISCONNECTED"
                    BillingClient.BillingResponse.ITEM_UNAVAILABLE -> "ITEM_UNAVAILABLE"
                    BillingClient.BillingResponse.ITEM_NOT_OWNED -> "ITEM_NOT_OWNED"
                    BillingClient.BillingResponse.ITEM_ALREADY_OWNED -> "ITEM_ALREADY_OWNED"
                    BillingClient.BillingResponse.ERROR -> "ERROR"
                    BillingClient.BillingResponse.DEVELOPER_ERROR -> "DEVELOPER_ERROR"
                    BillingClient.BillingResponse.FEATURE_NOT_SUPPORTED -> "FEATURE_NOT_SUPPORTED"
                    BillingClient.BillingResponse.OK -> {
                        null/*Already Handled*/
                    }
                    else -> "$responseCode"
                }
                }"
            } else {
                allSubscriptionsSkuDetailsMLD.value = skuDetailsList

                subscribedDetailsMLD.value = billingClient.queryPurchases(BillingClient.SkuType.SUBS)

                val subscribed = subscribedDetailsMLD.value
                if (subscribed != null && skuDetailsList != null) {
                    availableSubscriptionsSkuDetailsMLD.value = skuDetailsList.filter { skuDetails ->
                        val purchased = subscribed.purchasesList.firstOrNull { element ->
                            element.sku == skuDetails.sku
                        }
                        purchased == null
                    }
                } else {
                    availableSubscriptionsSkuDetailsMLD.value = null
                }
            }
        }
    }

    fun querySUBSAsync() {
        // Needs two queries, one for INAPP and the other for SUBS. Seem there is no way to query both at the same time.
        // Creating a runnable from the request to use it inside our connection retry policy below
        val queryRequest = Runnable {

            val itemType = BillingClient.SkuType.SUBS
            val skuList = listOf(
                "subscription_10", "subscription_20"
            )

            // Query the purchase async
            val params: SkuDetailsParams.Builder = SkuDetailsParams.newBuilder()
            params
                .setSkusList(skuList)
                .setType(itemType)
            billingClient.querySkuDetailsAsync(params.build(), subscriptionsSkuDetailsResponseListener)
        }

        executeServiceRequest(queryRequest)
    }

    fun getSkuDetails(skuId: String): SkuDetails? {
        val inAppSkuDetailsList = allInAppsSkuDetailsMLD.value
        if (inAppSkuDetailsList != null) {
            inAppSkuDetailsList.forEach {
                if (it.sku == skuId)
                    return it
            }
        }

        val subsSkuDetails = allSubscriptionsSkuDetailsMLD.value
        if (subsSkuDetails != null) {
            subsSkuDetails.forEach {
                if (it.sku == skuId)
                    return it
            }
        }

        return null
    }

    fun consumePurchase(purchaseToken: String) {
        // Think consume fun is available from a 'Purchased' so need to retrieve this before consuming

        billingClient.consumeAsync(purchaseToken, { responseCode: Int, responsePurchaseToken: String ->

            if (responseCode == BillingClient.BillingResponse.OK) {
                // Reset purchased and available
                queryINAPPAsync()
                querySUBSAsync()
            }

            messageMLD.value = "Consumed ${purchaseToken} with responseCode ${
            when (responseCode) {
                BillingClient.BillingResponse.BILLING_UNAVAILABLE -> "BILLING_UNAVAILABLE"
                BillingClient.BillingResponse.USER_CANCELED -> "USER_CANCELED"
                BillingClient.BillingResponse.SERVICE_UNAVAILABLE -> "SERVICE_UNAVAILABLE"
                BillingClient.BillingResponse.SERVICE_DISCONNECTED -> "SERVICE_DISCONNECTED"
                BillingClient.BillingResponse.ITEM_UNAVAILABLE -> "ITEM_UNAVAILABLE"
                BillingClient.BillingResponse.ITEM_NOT_OWNED -> "ITEM_NOT_OWNED"
                BillingClient.BillingResponse.ITEM_ALREADY_OWNED -> "ITEM_ALREADY_OWNED"
                BillingClient.BillingResponse.ERROR -> "ERROR"
                BillingClient.BillingResponse.DEVELOPER_ERROR -> "DEVELOPER_ERROR"
                BillingClient.BillingResponse.FEATURE_NOT_SUPPORTED -> "FEATURE_NOT_SUPPORTED"
                BillingClient.BillingResponse.OK -> "OK"
                else -> "$responseCode"
            }} and responsePurchaseToken $responsePurchaseToken"

            Log.d("TempGroupIcon", messageMLD.value)
        })

    }

    fun handlePurchase(activity: Activity, skuId: String, @BillingClient.SkuType skuType: String) {
        val flowParams = BillingFlowParams.newBuilder()
            .setSku(skuId)
            .setType(skuType) // SkuType.SUB for subscription
            .build()
        val responseCode = billingClient.launchBillingFlow(activity, flowParams)

        messageMLD.value = "Billing clinet launchBillingFlow responded with responseCode ${
        when (responseCode) {
            BillingClient.BillingResponse.BILLING_UNAVAILABLE -> "BILLING_UNAVAILABLE"
            BillingClient.BillingResponse.USER_CANCELED -> "USER_CANCELED"
            BillingClient.BillingResponse.SERVICE_UNAVAILABLE -> "SERVICE_UNAVAILABLE"
            BillingClient.BillingResponse.SERVICE_DISCONNECTED -> "SERVICE_DISCONNECTED"
            BillingClient.BillingResponse.ITEM_UNAVAILABLE -> "ITEM_UNAVAILABLE"
            BillingClient.BillingResponse.ITEM_NOT_OWNED -> "ITEM_NOT_OWNED"
            BillingClient.BillingResponse.ITEM_ALREADY_OWNED -> "ITEM_ALREADY_OWNED"
            BillingClient.BillingResponse.ERROR -> "ERROR"
            BillingClient.BillingResponse.DEVELOPER_ERROR -> "DEVELOPER_ERROR"
            BillingClient.BillingResponse.FEATURE_NOT_SUPPORTED -> "FEATURE_NOT_SUPPORTED"
            BillingClient.BillingResponse.OK -> "OK"
            else -> "$responseCode"
        }}"

        Log.d(
            "TempGroupIcon",
            messageMLD.value
        )

    }

    private fun startServiceConnection(executeOnSuccess: Runnable? = null) {
        Log.d("TempGroupIcon", "inAppProductsVM billingClient startConnection")
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(@BillingClient.BillingResponse billingResponseCode: Int) {
                if (billingResponseCode == BillingClient.BillingResponse.OK) {
                    isServiceConnectedMLD.value = true
                    executeOnSuccess?.run()
                    Log.d("TempGroupIcon", "inAppProductsVM onBillingSetupFinished with billingResponseCode 'OK'")

                } else {
                    messageMLD.value = "inAppProductsVM onBillingSetupFinished responded with responseCode ${
                    when (billingResponseCode) {
                        BillingClient.BillingResponse.BILLING_UNAVAILABLE -> "BILLING_UNAVAILABLE"
                        BillingClient.BillingResponse.USER_CANCELED -> "USER_CANCELED"
                        BillingClient.BillingResponse.SERVICE_UNAVAILABLE -> "SERVICE_UNAVAILABLE"
                        BillingClient.BillingResponse.SERVICE_DISCONNECTED -> "SERVICE_DISCONNECTED"
                        BillingClient.BillingResponse.ITEM_UNAVAILABLE -> "ITEM_UNAVAILABLE"
                        BillingClient.BillingResponse.ITEM_NOT_OWNED -> "ITEM_NOT_OWNED"
                        BillingClient.BillingResponse.ITEM_ALREADY_OWNED -> "ITEM_ALREADY_OWNED"
                        BillingClient.BillingResponse.ERROR -> "ERROR"
                        BillingClient.BillingResponse.DEVELOPER_ERROR -> "DEVELOPER_ERROR"
                        BillingClient.BillingResponse.FEATURE_NOT_SUPPORTED -> "FEATURE_NOT_SUPPORTED"
                        else -> " unanticipated '$billingResponseCode'"
                    }}"

                    Log.d(
                        "TempGroupIcon",
                        messageMLD.value
                    )

                }
            }

            override fun onBillingServiceDisconnected() {
                isServiceConnectedMLD.value = false
                Log.d("TempGroupIcon", "inAppProductsVM onBillingServiceDisconnected")
            }
        })
    }

    private fun executeServiceRequest(runnable: Runnable) {
        if (isServiceConnectedMLD.value ?: false) {
            runnable.run()
        } else {
            // If billing service was disconnected, we try to reconnect 1 time.
            // (feel free to introduce your retry policy here).
            startServiceConnection(runnable)
        }
    }

    fun connectBillingClient() {
        // No such thing as reconnecting billingClient, have to recreate and start the new connection
        createAndStartBillingClient()
    }

    fun disconnectBillingClient() {
        Log.d("TempGroupIcon", "calling billingClient endConnection")
        billingClient.endConnection()


        // The function 'endConnection' will not call 'onBillingServiceDisconnected'. To let the rest of the App know that billingClient is Disconnected, isServiceConnectedMLD will get updated to 'false'
        isServiceConnectedMLD.value = false
    }

}