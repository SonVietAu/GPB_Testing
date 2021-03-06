package hayqua.subscriptionstesting

import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import com.android.billingclient.api.SkuDetails

/**
 * Created by Son Au on 23/06/2018.
 */

// Display and Control: Subscription("Small and Ready", "Organisation with up to 10 staffs", "", "Subs", "Yearly", 10f),

class SubscriptionRow(
        mainActivity: MainActivity,
        val skuDetails: SkuDetails) : FrameLayout(mainActivity) {

    private val mainView = mainActivity.layoutInflater.inflate(R.layout.available_product_row, this, true)

    init {
        populateView()

        val purchaseBtn = mainView.findViewById<Button>(R.id.purchaseBtn)
        purchaseBtn.text = "Subscribe"
        purchaseBtn.setOnClickListener {
            mainActivity.inAppProductsViewModel.handlePurchase(mainActivity, skuDetails.sku, skuDetails.type)
        }
    }

    private fun populateView() {
        mainView.findViewById<TextView>(R.id.productTitleTV).text = skuDetails.title
        mainView.findViewById<TextView>(R.id.descriptionTV).text = skuDetails.description
        mainView.findViewById<TextView>(R.id.priceTV).text = "${skuDetails.price} ${skuDetails.subscriptionPeriod}"
    }
}