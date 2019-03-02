package hayqua.subscriptionstesting

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
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
        findViewById<View>(R.id.testPlayStoreDeepLink).setOnClickListener {
            val uriBuilder = Uri.parse("https://play.google.com/store/account/subscriptions")
                .buildUpon()
                // The random sku will open Play Store Subscriptions with a 'Subscription Not Found' error
                //.appendQueryParameter("sku", "android.test.random_sku_sdjf")
                .appendQueryParameter("sku", "subscription_10")
                .appendQueryParameter("package", application.packageName)

            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = uriBuilder.build()
            }
            startActivity(intent)
        }

        connectionBtn.setOnClickListener {
            if (inAppProductsViewModel.isServiceConnectedMLD.value ?: false)
                inAppProductsViewModel.disconnectBillingClient()
            else
                inAppProductsViewModel.connectBillingClient()
        }

        inAppProductsViewModel.messageMLD.observe(this, Observer {
            if (it != null)
                showInfoMessage(it)
            else
                showInfoMessage("No Messages")
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

                staticTestingsTL.visibility = View.VISIBLE

                staticTestingsErrorDisplay.visibility = View.GONE
                inAppsPurchasedErrorDisplay.visibility = View.GONE
                inAppsAvailableErrorDisplay.visibility = View.GONE
                subscribedErrorDisplay.visibility = View.GONE
                subsAvailableErrorDisplay.visibility = View.GONE

                hideMessageTV()
                connectionBtn.text = "End Billing Client"
            } else {
                // should display is not connected and remove all in-app and subs
                inAppsPurchasedLL.removeAllViews()
                inAppsAvailableLL.removeAllViews()
                subscribedLL.removeAllViews()
                subsAvailableLL.removeAllViews()

                staticTestingsTL.visibility = View.GONE

                staticTestingsErrorDisplay.visibility = View.VISIBLE
                inAppsPurchasedErrorDisplay.visibility = View.VISIBLE
                inAppsAvailableErrorDisplay.visibility = View.VISIBLE
                subscribedErrorDisplay.visibility = View.VISIBLE
                subsAvailableErrorDisplay.visibility = View.VISIBLE

                showInfoMessage("Billing Services Ended")

                connectionBtn.text = "Rebuild Billing Client"

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
