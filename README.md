📱 TwoFactorAuthApp – 2FA (TOTP) Android აპლიკაცია

**TwoFactorAuthApp** არის Android-ზე შექმნილი ორ-ფაქტორიანი ავთენტიფიკაციის (2FA) აპლიკაცია, რომელიც იყენებს TOTP (Time-based One-Time Password) ალგორითმს უსაფრთხო კოდების გენერირებისთვის.
აპლიკაცია შექმნილია Kotlin ენაზე და მოიცავს უსაფრთხო მონაცემთა შენახვისა და ავთენტიფიკაციის ძირითად მექანიზმებს.


ძირითადი ფუნქციები

TOTP კოდების გენერაცია რეალურ დროში (RFC 6238-ის შესაბამისად)
ანგარიშების დამატება QR კოდის სკანირებით ან ხელით
QR კოდის გენერაცია და ვიზუალიზაცია
მონაცემთა დაშიფვრა AES-256-ით `EncryptedRoom`-ით
Action Log – ყველა აქტივობის ჩაწერა (Created, Deleted, Accessed)
Material Design 3 UI + Custom Toolbar და FAB


ტექნოლოგიები და ბიბლიოთეკები

**Kotlin** + **Jetpack Navigation**
**Room Database** (Encrypted)
**ZXing** QR სკანერი / გენერატორი
**ViewBinding** / **LiveData** / **Coroutines**
**MVVM სტრუქტურა**
**Custom RecyclerView + ListAdapter**


შენიშვნა

აპლიკაცია არ ინახავს მონაცემებს ღია ტექსტში – ყველა ანგარიში იშიფრება **AES-ის მეშვეობით** და ინახება მხოლოდ ადგილობრივად.
