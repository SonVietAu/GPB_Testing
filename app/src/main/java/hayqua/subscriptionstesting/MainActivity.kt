package hayqua.subscriptionstesting

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.android.billingclient.api.BillingClient
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    val inAppProductsViewModel by lazy { ViewModelProviders.of(this).get(InAppProductsViewModel::class.java) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<View>(R.id.testP).setOnClickListener {
            inAppProductsViewModel.handlePurchase(this, "android.test.purchased", BillingClient.SkuType.INAPP)
        }
        findViewById<View>(R.id.testPConsume).setOnClickListener {
            inAppProductsViewModel.consumePurchase("android.test.purchased")
        }
        findViewById<View>(R.id.testC).setOnClickListener {
            inAppProductsViewModel.handlePurchase(this, "android.test.canceled", BillingClient.SkuType.INAPP)
        }
        findViewById<View>(R.id.testU).setOnClickListener {
            inAppProductsViewModel.handlePurchase(this, "android.test.item_unavailable", BillingClient.SkuType.INAPP)
        }
        findViewById<View>(R.id.testRandomSku).setOnClickListener {
            inAppProductsViewModel.handlePurchase(this, "android.test.random_sku_sdjf", BillingClient.SkuType.INAPP)
        }

        inAppProductsViewModel.messageMLD.observe(this, Observer {
            if (it != null)
                showInfoMessage(it)
            else
                showInfoMessage("No Messages")
                //hideMessageTV()
        })

        inAppProductsViewModel.inAppsPurchasedMLD.observe(this, Observer {
            // empty GUI then display InApp list
            inAppsPurchasedLL.removeAllViews()
            if (it != null && it.purchasesList.isNotEmpty()) {
                for (purchased in it.purchasesList) {
                    val skuDetails = inAppProductsViewModel.getSkuDetails(purchased.sku)
                    val purchasedRow = PurchasedRow(this, purchased, BillingClient.SkuType.INAPP, skuDetails)
                    inAppsPurchasedLL.addView(purchasedRow)
                }
            } else {
                val textView = TextView(this)
                textView.text = "No In Apps Purchased"
                textView.setTextColor(Color.rgb(68, 68, 68))
                inAppsPurchasedLL.addView(textView)
            }
        })

        inAppProductsViewModel.availableInAppsSkuDetailsMLD.observe(this, Observer {
            // empty GUI then display InApp list
            inAppsAvailableLL.removeAllViews()
            if (it != null && !it.isEmpty()) {
                for (skuDetails in it) {
                    val inAppRow = InAppRow(this, skuDetails)
                    inAppsAvailableLL.addView(inAppRow)
                }
            } else {
                val textView = TextView(this)
                textView.text = "No In Apps available"
                textView.setTextColor(Color.rgb(68, 68, 68))
                inAppsAvailableLL.addView(textView)
            }
        })

        inAppProductsViewModel.subscribedDetailsMLD.observe(this, Observer {
            // empty GUI then display Subs list
            subscribedLL.removeAllViews()
            if (it != null && it.purchasesList.isNotEmpty()) {
                for (purchased in it.purchasesList) {
                    val skuDetails = inAppProductsViewModel.getSkuDetails(purchased.sku)
                    val purchasedRow = PurchasedRow(this, purchased, BillingClient.SkuType.SUBS, skuDetails)
                    subscribedLL.addView(purchasedRow)
                }
            } else {
                val textView = TextView(this)
                textView.text = "No current subscriptions"
                textView.setTextColor(Color.rgb(68, 68, 68))
                subscribedLL.addView(textView)
            }
        })

        inAppProductsViewModel.availableSubscriptionsSkuDetailsMLD.observe(this, Observer {
            // empty GUI then display Subs list
            subsAvailableLL.removeAllViews()
            if (it != null && !it.isEmpty()) {
                for (skuDetails in it) {
                    val subscriptionRow = SubscriptionRow(this, skuDetails)
                    subsAvailableLL.addView(subscriptionRow)
                }
            } else {
                val textView = TextView(this)
                textView.text = "No subscriptions available"
                textView.setTextColor(Color.rgb(68, 68, 68))
                subsAvailableLL.addView(textView)
            }
        })

        inAppProductsViewModel.isServiceConnectedMLD.observe(this, Observer {
            if (it ?: false) {
                inAppProductsViewModel.queryINAPPAsync()
                inAppProductsViewModel.querySUBSAsync()
            } else {
                // should display is not connected and remove all inappa and subs
                showInfoMessage("Billing Services Disconnected")
            }
        })

    }

    private fun hideMessageTV() {
        findViewById<View>(R.id.messageTV)?.visibility = View.GONE
    }

    private fun showInfoMessage(message: String) {
        val messageTV = findViewById<View>(R.id.messageTV) as TextView
        messageTV.visibility = View.VISIBLE

        messageTV.text = message

    }

}
