# GameBoost Pro - APK Build Guide

## دليل بناء ملف APK

### المتطلبات الأساسية:
1. **Android Studio** (أحدث إصدار)
2. **Java JDK 17** أو أحدث
3. **Android SDK** مع API Level 34
4. **Gradle 8.0** أو أحدث

### خطوات بناء ملف APK:

#### 1. إعداد البيئة
```bash
# تأكد من تثبيت Java JDK 17
java -version

# تأكد من تثبيت Android SDK
echo $ANDROID_HOME
```

#### 2. فتح المشروع
1. فك ضغط ملف `GameBoostPro_Fixed_Final.tar.gz`
2. افتح Android Studio
3. اختر "Open an Existing Project"
4. حدد مجلد `GameBoostPro`

#### 3. إعداد Firebase (اختياري)
1. اذهب إلى [Firebase Console](https://console.firebase.google.com/)
2. أنشئ مشروع جديد أو استخدم مشروع موجود
3. أضف تطبيق Android بـ Package Name: `com.gameboost.pro`
4. حمل ملف `google-services.json` الحقيقي
5. استبدل الملف الموجود في `app/google-services.json`

#### 4. بناء ملف APK

##### من Android Studio:
1. اذهب إلى `Build` → `Build Bundle(s) / APK(s)` → `Build APK(s)`
2. انتظر اكتمال عملية البناء
3. ستجد ملف APK في: `app/build/outputs/apk/debug/app-debug.apk`

##### من سطر الأوامر:
```bash
# انتقل إلى مجلد المشروع
cd GameBoostPro

# بناء APK للتطوير
./gradlew assembleDebug

# بناء APK للإنتاج (موقع)
./gradlew assembleRelease
```

#### 5. مواقع ملفات APK:
- **Debug APK**: `app/build/outputs/apk/debug/app-debug.apk`
- **Release APK**: `app/build/outputs/apk/release/app-release.apk`

### حل المشاكل الشائعة:

#### مشكلة 1: Gradle Sync Failed
```bash
# تنظيف المشروع
./gradlew clean

# إعادة بناء المشروع
./gradlew build
```

#### مشكلة 2: Missing Android SDK
1. افتح Android Studio
2. اذهب إلى `Tools` → `SDK Manager`
3. تأكد من تثبيت:
   - Android SDK Platform 34
   - Android SDK Build-Tools 34.0.0
   - Android SDK Platform-Tools

#### مشكلة 3: Java Version Mismatch
```bash
# تحديد Java 17 كإصدار افتراضي
export JAVA_HOME=/path/to/java-17
```

#### مشكلة 4: Firebase Configuration
- إذا لم تكن تريد استخدام Firebase، احذف السطر التالي من `app/build.gradle`:
```gradle
id 'com.google.gms.google-services'
```

### إعدادات إضافية:

#### تخصيص معلومات التطبيق:
في ملف `app/build.gradle`:
```gradle
defaultConfig {
    applicationId "com.gameboost.pro"
    versionCode 1
    versionName "1.0"
    // يمكنك تغيير هذه القيم حسب الحاجة
}
```

#### إضافة توقيع للإنتاج:
```gradle
android {
    signingConfigs {
        release {
            storeFile file('path/to/keystore.jks')
            storePassword 'store_password'
            keyAlias 'key_alias'
            keyPassword 'key_password'
        }
    }
    buildTypes {
        release {
            signingConfig signingConfigs.release
            minifyEnabled true
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        }
    }
}
```

### اختبار APK:

#### 1. تثبيت على جهاز Android:
```bash
# تأكد من تفعيل Developer Options و USB Debugging
adb install app-debug.apk
```

#### 2. اختبار الميزات:
- ✅ تسجيل الدخول بـ Google (يتطلب Firebase صحيح)
- ✅ تبديل اللغة
- ✅ اختيار الخادم
- ✅ شاشة العرض
- ✅ استقرار الاتصال

### ملاحظات مهمة:

1. **الأذونات**: التطبيق يحتاج أذونات VPN، تأكد من منحها عند التثبيت
2. **Firebase**: للحصول على تسجيل دخول Google كامل، تحتاج إعداد Firebase حقيقي
3. **الاختبار**: اختبر جميع الميزات قبل التوزيع
4. **الأمان**: لا تشارك ملفات التوقيع أو كلمات المرور

### الدعم:
إذا واجهت أي مشاكل في البناء، تأكد من:
- تحديث Android Studio لأحدث إصدار
- تنظيف المشروع (`Build` → `Clean Project`)
- إعادة مزامنة Gradle (`File` → `Sync Project with Gradle Files`)

---

**ملف APK النهائي سيكون جاهزاً للتثبيت والاستخدام مع جميع الإصلاحات والميزات الجديدة!** 🚀

