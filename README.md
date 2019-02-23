# Android Billing Client Testing

## Introduction
To determine if an implementation of a Google Play Billing (GBP) in the initial development phase is functioning correctly, a little overhead must be completed in the Google Play Console. The little overhead consists of publishing an application and creating in-app products. Due to the complexity of any Android application, publishing a project in the development phase is deemed (by this author) a waste of effort. Bugs and debugging is (potentially) nonexistent when running the App from publication. Hence, this project was created to remove all the complexities of Android projects and only test this implementation of Google Play Billing. Once completed and successful, other projects can extract from this project and hopefully have a simpler implementation of Google Play Billing.

## Objectives
Implement an Android Mobile App that is able to show and handle purchases of in-app products, both the Managed Products and Subscriptions<sup>1</sup>. The App will show available products along with purchased.

As this project is designed to support development, the App will also demonstrate purchases of 'reserved product IDs'.

This project also aim to highlight all the nitty-gritties that are not **explicitly** stated in the Google Developer Document.

## Architecture
Though the architecture is not at all important for this particular initial version of this App, the other projects (this author's projects) that will extract works from this project are using MVVM. This project will follow the MVVM recommendations.

With this version handling the basics of Google Play Billing, this version is limited to an Activity (the view) and the InAppProductsViewModel.

## Implementation
### Initial App Publication
Although this step is not absolutely needed till later in the development cycle, the creation of in-app products is much easier with a minimal of an upload of an APK/aab. The minimal is to declare the use of 'com.android.vending.BILLING' in the AndroidManifest. The permission and upload is required to start creating in-app products in Google Play Console.

There are many elements of in-app products that need to be defined and for this testing project, the elements are much easier to define via the Google Play Console forms. The form however, have fields that cannot be change ('Product ID' and subscription's 'Status') and cannot be reuse ('Product Id'). The naming of 'Product ID' also follow a particular convention. Should in-app products be defined outside the form, cautions need to be taken as the IDs can not be changed and reused.

### BillingClient Initiation
Apart from the 'server verification of purchases', all the interaction to Google Play Billing is done via an instance of the BillingClient. Getting such an instance (**billingClient**) is simple and the instance is built in the initiation of an 'InAppProductsViewModel'. The building of the billingClient requires an instance of a PurchasesUpdatedListener and an implementation of the function onPurchasesUpdated. There is currently little use of the PurchasesUpdatedListener but the listener is very likely to be important in later version of this project to handle the like of use 'promo code in the Play Store'.

Once the billingClient is built, it requires the App to call the function 'startConnection'. A required parameter to the function is an instance of a BillingClientStateListener. The state listener will handle 'onBillingSetupFinished' and 'onBillingServiceDisconnected'. 'onBillingSetupFinished' returns one of the many '@BillingClient.BillingResponse' codes. Of those codes, the 'BILLING_UNAVAILABLE' code was encountered. The encounter was due to the development device not logging in a user to the Google Play Store. Once logged in, subsequence calls to 'startConnection' return return the 'OK' code and the billingClient is then ready to handle purchases and other interaction.

'onBillingServiceDisconnected' means that the billingClient is not able to handle any interaction till the callback of 'onBillingSetupFinished' is made. Google document indicates that callback on 'onBillingServiceDisconnected' will happen when the Play Store is updating. The document have not indicated any other reasons for a callback on 'onBillingServiceDisconnected'.

### Static Testings
Once the billingClient is setup and connection is 'OK', the static testings can begin. Static testings are simple, they are simply a build of a BillingFlowParams with a productId/SKU string and a call to the function 'launchBillingFlow' of the billingClient. The Google Billing Flow will handle the purchasing till 'onPurchasesUpdated' is callback on the PurchasesUpdatedListener setup earlier. The three available SKUs for static testings are 'android.test.purchased', 'android.test.canceled' and 'android.test.item_unavailable'. Successful handling of the three SKU should match the document descriptions (see 'Test Google Play Billing' web page).

Though not specifically stated in the Static testings part of the document but a second or later request to purchase 'android.test.purchased' will result in 'ITEM_ALREADY_OWNED'. To allow for more purchases of 'android.test.purchased', the consume use-case was added and executed with the call to consumeAsync on the billingClient. The call requires a ConsumeResponseListener and the listener can handle a responseCode and responsePurchaseToken if required.

Of course there would be no testings without the handling of errors. The random SKU static testing was added for this purpose and the response is simply and predictably an 'ERROR'.

### Querying and Displaying of Purchased, Subscribed and Available
Querying for list of purchased is simple, requiring only a call to 'queryPurchases' on the billingClient with a SkuType (either INAPP or SUBS). The execution of 'queryPurchases' is synchronous and returns a 'Purchase.PurchasesResult'. The returned 'Purchase.PurchasesResult' contains a 'purchasesList' that can be looped and display to the view. However, information in the purchasesList is rather lacking, the information does not include product's title and description that maybe very useful to users. To display the title and description, a SkuDetails matched by the SKU id was included as a parameter along with each instance of 'Purchase'. There is a potential for no SkuDetails to match (eg not queried for in the App or Google Play Billing request failed), for this version, 'Product Information Unavailable' will be displayed.

Querying for a list of available requires a little more preparations: an instance ('params') of 'SkuDetailsParams.Builder' must be prepared and built. To complete the 'params', a 'BillingClient.SkuType' must be set along with a 'SkusList'. To completed the 'SkusList', SKUs for all in-app products and subscriptions must be defined and entered into Google Play Console. Any correct SKUs not listed will cause their 'SkuDetails' to be missing from the result and any incorrect SKUs listed will be ignored. To query for a correct and completed complete list of available, the SKUs listed can be downloaded from Google Play Console and formatted into the required 'SkusList'.

Google Play Billing treats in-app products and subscriptions as two different products and can not be queried in one request. The query of each product must be prepared separately with different 'SkusList' along with the correct 'SkuType'.

Once preparation is completed, querying for available is simply a call to 'querySkuDetailsAsync' on the billingClient. The function is asynchronous and the resulting responseCode and list of 'SkuDetails' will be returned via a 'SkuDetailsResponseListener' in the callback 'onSkuDetailsResponse'. Should there be no errors, the list is ready to be displayed.

### Handling Purchasing
Handling a purchasing or a subscribing is simply a call to 'launchBillingFlow' of the billingClient with an build ('flowParams') of 'BillingFlowParams'. The build requires the SKU and SkyType from the purchasing or subscribing product. As the name of the function indicates, the call only launch a billing flow. The actual process of making the purchase or subscribing is handled entirely by Google Play. The result of the process is returned to the 'PurchasesUpdatedListener' set up on the initial 'build' of the billingClient. For this particular version of the project, a message of the purchasing/subscribing result will simply be display and the purchased, subscribed and available lists get refreshed.

### Live Testing
Though displaying of available products can be done on development/debug running of this App, the mock purchasing or subscribing can not be done on such running. Attempts to make such purchases or subscriptions resulted in a particular error with the message 'the item you requested is not available for purchase'. Google Document stated that such purchasing or subscribing can be completed on a 'closed testing track'. Tests on the 'closed testing track' **have** and will result in real accounts getting deducted.

## Summary
This initial implementation was completed successfully. Completing the overheads of publishing and creating products via Google Play Console enabled comprehensive but not complete testing on development device. The nitty-gritties of asynchronous calls, defining 'Product ID' and getting the 'SkuList' will need close monitoring. Finally, successful displaying of all in-app products and subscriptions along with a successful purchase of the static testing 'android.test.purchased' will ensure successful purchasing or subscribing on running of published App.

## Testing the App
If anyone would like to try this App on a 'closed testing track', please email ddmobileapp@gmail.com for the Play Store link. **Warning: real account will be deducted for purchase and subscriptions**.

## Next Steps
There are still a more requirements that will need completing to consider this GPB to be wholly successful. In the next version or versions to come, the following amongst others will be added: better handling of billingClient connection, refund, promo code, downgrade, upgrade, and server verifications.

## Author's Request for Comments
1. Is there any details that I have missed and should have been included in this version?

## Footnote
1. It seem that Google Document refers to 'in-app' and 'managed' products to mean all the non-subscription purchasable products and at the same time, the 'in-app' products also mean both the non-subscription purchasable and the subscriptions.
