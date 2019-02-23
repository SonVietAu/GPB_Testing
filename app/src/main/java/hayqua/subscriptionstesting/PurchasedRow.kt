package hayqua.subscriptionstesting

import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.SkuDetails

/**
 * Created by Son Au on 23/06/2018.
 */

// Display and Control: Subscription("Small and Ready", "Organisation with up to 10 staffs", "", "Subs", "Yearly", 10f),

class PurchasedRow(
        mainActivity: MainActivity,
        val purchase: Purchase,
        @BillingClient.SkuType val skuType: String,
        var skuDetails: SkuDetails?) : FrameLayout(mainActivity) {

    private val mainView = mainActivity.layoutInflater.inflate(R.layout.available_product_row, this, true)

    init {
        populateView()

        val purchaseBtn = mainView.findViewById<Button>(R.id.purchaseBtn)
        if (skuType == BillingClient.SkuType.INAPP) {
            purchaseBtn.text = "Consume"
            purchaseBtn.setOnClickListener {
                mainActivity.inAppProductsViewModel.consumePurchase(purchase.purchaseToken)
            }
        } else {
            purchaseBtn.text = "Unsubscribe"
            purchaseBtn.setOnClickListener {
                mainActivity.inAppProductsViewModel.cancelSubscription(purchase.purchaseToken)
            }
        }
    }

    private fun populateView() {
        mainView.findViewById<TextView>(R.id.productTitleTV).text = skuDetails?.title ?: "Product Information Unavailable"
        mainView.findViewById<TextView>(R.id.descriptionTV).text = skuDetails?.description
        if (skuType == BillingClient.SkuType.SUBS) {
            val price = "${skuDetails?.price} ${skuDetails?.subscriptionPeriod}${if (purchase.isAutoRenewing) " (AutoRenewing)" else ""}"
            mainView.findViewById<TextView>(R.id.priceTV).text = price
        } else {
            mainView.findViewById<TextView>(R.id.priceTV).visibility = View.GONE
        }
    }

}