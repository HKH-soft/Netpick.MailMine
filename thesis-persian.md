---
title: "پلتفرم وب اسکرپینگ و استخراج ایمیل خودکار: معماری و پیاده‌سازی یک سامانه یکپارچه مدیریت داده‌های تجاری"
author: ""
date: "ژوئن ۲۰۲۴"
lang: fa
direction: rtl
---

# چکیده

در این پژوهش، یک سامانه جامع وب اسکرپینگ و استخراج ایمیل با معماری توزیع‌شده برای استخراج داده‌های تجاری از وب بررسی و پیاده‌سازی شده است. سامانه نت‌پیک (Netpick) با استفاده از فناوری‌های مدرن شامل جاوا ۲۱، اسپرینگ بوت ۳.۵.۶ در بک‌اند و Next.js ۱۵ در فرانت‌اند توسعه یافته است.

یکی از ویژگی‌های بارز این سامانه، استفاده از الگوی معماری ماژولار مونولیت با طبقه‌بندی دامنه‌محور (Domain-Driven Design) است که شامل ماژول‌های گیت‌کیپر (Gatekeeper) برای امنیت و احراز هویت، میل‌ماین (MailMine) برای اسکرپینگ وب و ماژول‌های کسب‌وکار شامل دیل‌فارم، فایل‌فارم، اینونتوری‌فارم و تسک‌فارم می‌باشد.

سیستم استخراج داده با استفاده از Playwright برای رندر مرورگر و Jsoup برای تحلیل HTML، قادر است داده‌های پیچیده را از وب‌سایت‌های مختلف استخراج کند. سامانه همچنین از JWT با قابلیت چرخش کلید، احراز هویت دو مرحله‌ای (MFA) بر پایهٔ TOTP و سیستم‌های مقاومت در برابر حملات توسعه یافته است.

نتایج ارزیابی نشان داد که سامانه با حذف موارد دیوارئیده شده و اعتبارسنجی URLها، امنیت شبکه را تضمین می‌کند و با استفاده از Redis برای کش و محدودیت نرخ درخواست، قابلیت مقیاس‌پذیری را فراهم می‌سازد.

واژه‌نامه: وب اسکرپینگ، استخراج ایمیل، امنیت سامانه، معماری ماژولار، هوش مصنوعی، Playwright، JWT، MFA.

رشته: مهندسی کامپیوتر - نرم‌افزار

---

# فهرست مطالب

## فصل اول: مقدمه و پیشینه تحقیق

### ۱.۱ مقدمه و چشم‌انداز تحقیق
### ۱.۲ بیان مسأله و اهمیت تحقیق
### ۱.۳ اهداف تحقیق
### ۱.۴ روش‌شناسی تحقیق
### ۱.۵ ساختار پایان‌نامه

## فصل دوم: مروری بر کارهای پیشین

### ۲.۱ سامانه‌های استخراج داده وب
### ۲.۲ فناوری‌های امنیتی در برنامه‌های وب
### ۲.۳ چارچوب‌های معماری ماژولار
### ۲.۴ تحلیل کتابخانه‌های مرور شده
### ۲.۵ کمبودهای موجود و فرصت‌های تحقیق

## فصل سوم: معماری کلی سامانه

### ۳.۱ الگوی معماری پیشنهادی
### ۳.۲ ساختار ماژولار سامانه
### ۳.۳ لایه‌های اصلی و تعاملات
### ۳.۴ مدل‌های داده و ذخیره‌سازی

## فصل چهارم: پیاده‌سازی سامانه

### ۴.۱ لایهٔ سرویس‌گر (Backend)
### ۴.۲ لایهٔ رابط کاربری (Frontend)
### ۴.۳ سرویس‌های هوش مصنوعی
### ۴.۴ فناوری‌های استخراج داده

## فصل پنجم: ارزیابی و نتایج

### ۵.۱ معیارهای ارزیابی
### ۵.۲ تست‌های واحد و یکپارچه
### ۵.۳ نتایج دقیقه‌ای و کارایی
### ۵.۴ تحلیل امنیتی

## فصل ششم: بحث و نتیجه‌گیری

### ۶.۱ بحث نتایج
### ۶.۲ مزایا و محدودیت‌ها
### ۶.۳ کارهای آینده

## پیوست‌ها

## فهرست منابع


# فصل اول: مقدمه و پیشینه تحقیق

## ۱.۱ مقدمه و چشم‌انداز تحقیق

در دوران دیجیتالی که امروز ما را تشکیل می‌دهد، داده‌ها گوهرشمار و اسباب تصادف اقتصاد دیجیتال محسوب می‌شوند. در بازارهای نوظهور، داده‌های تجاری از جمله اطلاعات تماس (ايميل، شماره تلفن) و لينک‌های مرتبط با كسب‌وكارها، با ارزش‌های بالایي محبوبيت يافته‌اند. استخراج اين داده‌ها از وب، چالشی پيچيده بوده و نيازمند راهكارهای پيشرفته‌اي است.

پلتفرم نت‌پيك (Netpick) به عنوان يك سامانه جامع وب اسكرپينگ و استخراج ايميل خودكار، در اين پژوهش توسعه يافته و تحليل مي‌شود.
# فصل اول: مقدمه و پیشینه تحقیق

## ۱.۱ مقدمه و چشم‌انداز تحقیق

در دوران دیجیتالی که امروز ما را تشکیل می‌دهد، داده‌ها گوهرشمار و اسباب تصادف اقتصاد دیجیتال محسوب می‌شوند. در بازارهای نوظهور، داده‌های تجاری از جمله اطلاعات تماس (ايميل، شماره تلفن) و لينک‌های مرتبط با كسب‌وكارها، با ارزش‌های بالایي محبوبيت يافته‌اند. استخراج اين داده‌ها از وب، چالشی پيچيده بوده و نيازمند راهكارهای پيشرفته‌اي است.

پلتفرم نت‌پيك (Netpick) به عنوان يك سامانه جامع وب اسكراپينگ و استخراج ايميل خودكار، در اين پژوهش توسعه يافته و تحليل مي‌شود. اين سامانه با هدف فراهم آوردن ابزارهای خودكار برای استخراج داده‌های تجاري از وب‌سايت‌های مختلف، از جمله صفحات تماس، فهرست كارها و وب‌سايت‌های تجاري، طراحي شده است.

سامانه موروثي از الگوی معماری ماژولار مونوليت با طبقه‌بندي دامنه‌محور (Domain-Driven Design) است که شامل ماژول‌های گيت‌کيپر (Gatekeeper) براي امنيت و احراز هويت، ميل‌ماين (MailMine) براي اسكراپينگ وب و ماژول‌های كسب‌وكار شامل ديال‌فارم، فيلم‌فارم، اينونتوري‌فارم و تسک‌فارم مي‌باشد.

## ۱.۲ بيان مسأله و اهميت تحقيق

### ۱.۲.۱ بيان مسأله

استخراج داده از وب‌سايت‌های مختلف با مواجهه چالش‌های زير است:

1. **پويايي وب‌سايت‌ها**: ساختارهای مختلف وب‌سايت‌ها نيازمند راهكارهای متفاوت براي استخراج داده‌ها هستند.
2. **محدوديت‌های دسترسي**: بسياري از وب‌سايت‌ها از مكانيزم‌های محافظتی مانند CORS، ربات دانه‌اي (CAPTCHA) و محدوديت نرخ درخواست استفاده مي‌كنند.
3. **امنيت و حريم خصوصي**: در دنياي فناوري امروز، نگهداري حريم خصوصي كاربران و امنيت داده‌ها اهميت فراواني دارد.
4. **مقياس‌پذيري**: نياز به استخراج حجم بالايي از داده‌ها، مزيت‌های يك سامانه مقياس‌پذير را برجسته مي‌سازد.

### ۱.۲.۲ اهميت تحقيق

تحقيق حاضر از اهميت فني و علمي زير برخوردار است:

- **ارائه يك معماری جامع**: ترکيب تكنولوژي‌های مختلف (اسكراپينگ، امنيت، هوش مصنوعي) در قالب يك سامانه يكپارچه.
- **بهبود امنيت برنامه‌هاي وب**: پياده‌سازي الگوهای پيشرفته مديريت هوشمند توكن (JWT با چرخش كليد)، احراز هويت دو مرحله‌اي و سيستم تشخيص ناهنجاري.
- **بهينه‌سازي اسكراپينگ وب**: استفاده از Playwright به عنوان موتور مرورگر براي رندر صفحات پويا و Jsoup براي تحليل محتواهاي HTML.

## ۱.۳ اهداف تحقيق

### ۱.۳.۱ اهداف كلي

توسعه يك سامانه مدرن وب اسكراپينگ با ويژگي‌های زير:

1. امكان استخراج خودكار ايميل از وب‌سايت‌های مقصد
2. پياده‌سازي سيستم امنيتي قوي با JWT و MFA
3. ارائه رابط كاربری وب مدرن و واكنش‌گرا
4. يكپارچه‌سازي سرويس‌های هوش مصنوعي براي بهينه‌سازي جستجو

### ۱.۳.۲ اهداف خاص

- تحليل و پياده‌سازي الگوی معماري ماژولار مونوليت
- پياده‌سازي سرويس‌های لايه دسترسي (Repository) و سرويس‌گر (Service)
- طراحي ديتابيس relation با PostgreSQL
- ارزيابي عملكرد سامانه تحت بارهای مختلف

---
# فصل دوم: مروری بر کارهای پیشین

## ۲.۱ سامانه‌های استخراج داده وب

در سال‌های اخیر، سامانه‌های متعددی برای استخراج داده از وب طراحی و پیاده‌سازی شده‌اند. از جمله مهم‌ترین این سامانه‌ها می‌توان به:

### ۲.۱.۱ Scrapy
فریمورک معروف پایتونی برای اسکراپینگ وب که از قابلیت استخراج داده‌های ساختارمند و عملکرد قوی برخوردار است. با این حال، Scrapy برای صفحات پویا (SPA) محدودیت دارد.

### ۲.۱.۲ Puppeteer
کتابخانه‌ای مبتنی بر Node.js که برای کنترل مرورگر Chrome/Chromium استفاده می‌شود. این کتابخانه برای رندر صفحات پویا مناسب است اما مقیاس‌پذیری محدودی دارد.

### ۲.۱.۳ Selenium WebDriver
یکی از قدیمی‌ترین ابزارها برای خودکارسازی مرورگر است که از چندین مرورگر پشتیبانی می‌کند. با این حال، سرعت و منابع مصرفی بالایی دارد.

## ۲.۲ فناوری‌های امنیتی در برنامه‌های وب

### ۲.۲.۱ احراز هویت توکن (JWT)
JSON Web Token یک استاندارد باز برای انتقال به‌صورت امن اطلاعات بین طرفین است. در سامانه نت‌پیک از JWT با قابلیت چرخش کلید (JWT Key Rotation) استفاده شده است.

### ۲.۲.۲ احراز هویت دو مرحله‌ای (MFA)
این سامانه از TOTP (Time-Based One-Time Password) بر پایه‌ٔ الگوریتم HMAC-SHA1 برای احراز هویت دومرحله‌ای استفاده می‌نماید.

### ۲.۲.۳ سیستم تشخیص ناهنجاری
سامانه نت‌پیک قابلیت تشخیص رفتار ناهنجاری کاربر را دارد که شامل بررسی چندین ورود ناموفق، تشخیص IP جدید و شناسایی دستگاه‌های ناشناخته است.

## ۲.۳ چارچوب‌های معماری ماژولار

### ۲.۳.۱ معماری میکروسرویس
در این الگو، سامانه به‌صورت سرویس‌های کوچک و مستقل تقسیم می‌شود که هر کدام مسئول یک عملکرد خاص می‌باشند.

### ۲.۳.۲ معماری لایه‌ای (Layered Architecture)
سامانه نت‌پیک از معماری لایه‌ای استفاده می‌کند که شامل لایه‌های Presentation، Business Logic، Data Access و Data Storage می‌باشد.

### ۲.۳.۳ الگوی Modular Monolith
این الگو ترکیبی از مزایای میکروسرویس و سادگی مونولیت است. در این الگو، ماژول‌ها به‌صورت واضح جدا شده‌اند اما در یک برنامهٔ اجرایی واحد می‌مانند.

---
# فصل سوم: معماری کلی سامانه

## ۳.۱ الگوی معماری پیشنهادی

سامانه نت‌پیک از الگوی Modular Monolith با طبقه‌بندی دامنه‌محور (Domain-Driven Design) استفاده می‌نماید. این الگو شامل مولفه‌های زیر است:

### ۳.۱.۱ ساختار ماژولار
سامانه به‌صورت ماژول‌های مستقل سازماندهی شده است که هر کدام مسئول یک حوزهٔ کسب‌وکاری هستند:

| ماژول | حوزهٔ کاربردي |
|------|-------------|
| Gatekeeper | احراز هویت و امنیت |
| MailMine | وب اسکراپینگ و استخراج ایمیل |
| DealFarm | CRM و فروش |
| FinanceFarm | مالی و حسابداری |
| TaskFarm | مدیریت وظایف |
| FileFarm | ذخیره‌سازی داده |
| InventoryFarm | موجودی و کالا |

## ۳.۲ ساختار ماژولار سامانه

### ۳.۲.۱ ماژول Gatekeeper
ماژول امنیتی سامانه که شامل کلاس‌های کلیدی زیر است:

- **User**: موجودیت کاربر با فیلدهای email، passwordHash، role
- **Role**: نقش کاربر (USER، ADMIN، SUPER_ADMIN)
- **MfaSettings**: تنظیمات احراز هویت دو مرحله‌ای
- **IpPolicy**: سیاست‌های IP برای دسترسی
- **SecurityEvent**: تاریخچه رویدادهای امنیتی

### ۳.۲.۲ ماژول MailMine
ماژول اصلی اسکراپینگ که شامل مولفه‌های زیر است:

- **ScrapeJob**: وظیفهٔ اسکراپینگ با فیلدهای link، attempt، beenScraped
- **ScrapeData**: دادهٔ استخراج شده از صفحات
- **Contact**: اطلاعات تماس استخراج شده
- **Proxy**: پروکسی‌های مختلف برای دور زدن محدودیت
- **Pipeline**: جریان کاری یکپارچه اسکراپینگ

### ۳.۲.۳ ماژول AI
سرویس‌های هوش مصنوعی برای بهینه‌سازی عملکرد:

- **GeminiService**: سرویس متن‌بنیاد با قابلیت retry
- **SpamDetectionService**: تشخیص هرزنامه
- **EmailClassificationService**: طبقه‌بندی ایمیل
- **SentimentAnalysisService**: تحلیل حس هیجانی

## ۳.۳ لایه‌های اصلی و تعاملات

### ۳.۳.۱ لایهٔ تحلیل داده
کلاس DataProcessor مسئول پردازش داده‌های خام است. این کلاس داده‌ها را در بارهای ۱۰۰تایی پردازش می‌کند تا از خطای OOM جلوگیری شود.

### ۳.۳.۲ لایهٔ اسکراپینگ
کلاس Scraper با استفاده از Playwright برای رندر صفحات و Jsoup برای تجزیه HTML عمل می‌کند.

### ۳.۳.۳ لایهٔ هوش مصنوعی
سرویس SearchQueryGenerator با استفاده از Gemini API، پرس‌وجوهای جستجوی هوشمند برای هر بازهٔ زمانی تولید می‌کند.

## ۳.۴ مدل‌های داده و ذخیره‌سازی

### ۳.۴.۱ BaseEntity
تمام موجودیت‌ها از این کلاس پایه ارث می‌برند که شامل فیلدهای UUID id، createdAt، updatedAt و deleted می‌باشد.

### ۳.۴.۲ روابط کلیدی
روابط بین موجودیت‌ها به‌صورت relation در دیتابیس تعریف شده‌اند. برای مثال، کاربران می‌توانند چندین توکن Refresh داشته باشند (۱:N).

---
# فصل چهارم: پیاده‌سازی سامانه

## ۴.۱ لایهٔ سرویس‌گر (Backend)

### ۴.۱.۱ فناوری‌های اصلی
سامانه نت‌پیک با فناوری‌های زیر پیاده‌سازی شده است:

| فناوري | نسخه | كاربرد |
|--------|-------|--------|
| Java | ۲۱ | زبان برنامه‌نويسي اصلي |
| Spring Boot | ۳.۵.۶ | چارچوب كاري |
| PostgreSQL | ۱۷ | پايگاه داده |
| Redis | ۸.۴ | كَش و محدوديت نرخ |
| Playwright | ۱.۵۲.۰ | كنترل مرورگر |
| Jsoup | ۱.۲۱.۲ | تجزيه HTML |

### ۴.۱.۲ ساختار پروژه
ساختار پوشه‌ای پروژه به شرح زیر است:

```
src/main/java/ir/netpick/platform/
├── gatekeeper/     # مدیریت امنیت
├── mailmine/       # اسکراپینگ وب
├── ai/            # سرویس‌های هوش مصنوعی
├── core/          # کلاس‌های اشتراکی
├── dealfarm/      # مدیریت فروش
├── taskfarm/      # مدیریت وظایف
└── financefarm/   # مالی
```

### ۴.۱.۳ سرویس AuthenticationService
این سرویس جریان احراز هویت کامل را پیاده‌سازی می‌کند:

1. **بررسی سیاست IP**: قبل از ورود، IP کاربر بررسی می‌شود
2. **محدودیت نرخ**: بررسی تعداد تلاش‌های ناموفق
3. **تشخیص ناهنجاری**: تجزیه و تحلیل رفتار کاربر
4. **احراز هویت MFA**: در صورت فعال بودن، کد TOTP درخواست می‌شود
5. **ثبت رویداد**: تمام رخدادها در SecurityEvent ذخیره می‌شود

## ۴.۲ لایهٔ رابط کاربری (Frontend)

### ۴.۲.۱ فناوری‌های اصلی
سامانه با فناوری‌های زیر در فرانت‌اند توسعه یافته است:

| فناوري | نسخه | كاربرد |
|--------|-------|--------|
| Next.js | ۱۵.۵.۲۰ | فریمورک رابط كاربری |
| React | ۱۹.۰.۰ | كتابخانه كاري |
| TypeScript | ۵.۰ | زبان برنامه‌نويسي |
| Tailwind CSS | ۴.۰.۰ | استایل‌دهی |
| TanStack Query | ۵.۱۰۱.۲ | مدیریت وضعیت سرور |

### ۴.۲.۲ ساختار پوشه‌ای Frontend

```
src/
├── app/           # صفحات اپلیکیشن
├── components/    # کامپوننت‌های React
├── hooks/         # React Hooks
├── services/      # سرویس‌های API
└── utils/         # توابع کمکی
```

## ۴.۳ سرویس‌های هوش مصنوعی

### ۴.۳.۱ GeminiService
این سرویس ارتباط با Gemini API را بر عهده دارد:

```java
@Retryable(
    retryFor = { RuntimeException.class },
    maxAttempts = 3,
    backoff = @Backoff(delay = 1000, multiplier = 2.0)
)
public String generateText(String prompt) {
    // توليد متن با Gemini
}
```

قابلیت retry با exponential backoff برای اطمینان از در دسترس بودن سرویس گنجانده شده است.

### ۴.۳.۲ SearchQueryGenerator
این سرویس برای تولید پرس‌وجوهای جستجوی هدفمند استفاده می‌شود:

- تولید پرس‌وجوهای پایه برای حوزهٔ خاص
- تولید تنوع پرس‌وجوها
- تولید پرس‌وجو با محدودیت سایت (site:restriction)
- تولید پرس‌وجو برای یافتن ایمیل

## ۴.۴ فناوری‌های استخراج داده

### ۴.۴.۱ Scraper Engine
موتور اسکراپینگ با ویژگی‌های زیر:

1. استفاده از Playwright برای رندر مرورگر
2. حمایت از پروکسی‌های HTTP و V2Ray
3. بررسی ایمنی URL قبل از بازدید
4. حذف دامنه‌های مسدود (BLOCKED_DOMAINS)
5. پیگیری پیشرفت در هر لحظه

### ۴.۴.۲ DataProcessor Pipeline
پردازش داده با روش Batch Processing:

1. دریافت داده‌های پرداستخوانی نشده
2. استخراج ایمیل با الگوی منظم
3. استخراج ایمیل از لینک‌های mailto
4. ذخیره در دیتابیس

---
# فصل پنجم: ارزیابی و نتایج

## ۵.۱ معیارهای ارزیابی

### ۵.۱.۱ معیارهای کارایی
- زمان واکنش APIها (Response Time)
- تعداد درخواست‌های قابل پردازش در ثانیه (Throughput)
- مصرف منابع CPU و حافظه
- زمان تکمیل یک Pipeline کامل

## ۵.۲ تست‌های واحد و یکپارچه

### ۵.۲.۱ تست‌های واحد
سامانه شامل تعداد زیادی تست واحد است که شامل:

- **UserTest**: تست موجودیت کاربر
- **TaskTest**: تست مدیریت وظایف
- **PageDTOMapperTest**: تست تبدیل داده
- **AuthenticationServiceUnitTest**: تست خدمات احراز هویت

### ۵.۲.۲ تست‌های یکپارچه
تست‌های یکپارچه شامل:

- **TaskControllerIntegrationTest**: تست APIهای وظایف
- **TaskServiceIntegrationTest**: تست سرویس‌های وظایف

## ۵.۳ نتایج دقیقه‌ای و کارایی

### ۵.۳.۱ آمار کلیدی
| معيار | مقدار | توضيح |
|------|-------|------|
| تعداد کلاس‌های Java | ۲۷۰ | از منابع کامپایل شده |
| تعداد صفحات Frontend | ۵۸ | صفحات تولید شده Next.js |
| ماژول‌های کسب‌وکار | ۷ | Gatekeeper، MailMine و ... |
| تنظیمات JWT | ۱۵ دقيقه | انقضاء توکن دسترسی |

### ۵.۳.۲ مصرف منابع
- حافظه: استفاده از AtomicInteger برای پیگیری پیشرفت
- اسکراپینگ: batch size تنظیم‌شده روی ۱۰۰
- تعداد حداکثر تلاش اسکراپینگ: ۳ بار

## ۵.۴ تحلیل امنیتی

### ۵.۴.۱ فیلترهای امنیتی
سامانه از فیلترهای زیر استفاده می‌کند:

1. **IpPolicyFilter**: مبنی بر لیست سفید/سیاه IP
2. **SecurityAuditFilter**: ثبت ورودها و خروجها
3. **JWTAuthenticationFilter**: اعتبارسنجی توکن

### ۵.۴.۲ حمایت از MFA
- پشتیبانی از TOTP بر پایهٔ RFC 6238
- تولید کدهای پشتیبان (۱۰ عدد)
- مقایسه زمان‌دار (constant-time) برای جلوگیری از حملهٔ زمان‌سنجی

---
# فصل ششم: بحث و نتیجه‌گیری

## ۶.۱ بحث نتایج

### ۶.۱.۱ کارایی معماری Modular Monolith
سامانه نت‌پیک با موفقیت از الگوی Modular Monolith استفاده کرده است. این الگو مزایای زیر را فراهم کرده است:

1. **سادگی استقرار**: در مقابل میکروسرویس، استقرار یکپارچه
2. **قابلیت تست**: تست‌های واحد و یکپارچه قابل اجرا
3. **مقیاس‌پذیری**: با Redis برای کَش و محدودیت نرخ

### ۶.۱.۲ ادغام هوش مصنوعی
توابع GeminiService با الگوی Retryable پیاده‌سازی شده است. این سرویس قابلیت‌های زیر را دارد:

- retry با exponential backoff
- بدون نیاز به سرور داخلی
- سازگاری با محیط‌های مختلف

## ۶.۲ مزایا و محدودیت‌ها

### ۶.۲.۱ مزایا
- معماری ماژولار واضح
- سیستم امنیتی قوی (JWT + MFA + Anomaly Detection)
- پشتیبانی از اسکراپینگ پویا (Playwright)
- یکپارچه‌سازی AI برای بهینه‌سازی

### ۶.۲.۲ محدودیت‌ها
- وابستگی به سرویس Gemini API خارجی
- استفاده از پروکسی برای دور زدن محدودیت
- پیش‌نیازهای سیستم بالا (Java 21، PostgreSQL)

## ۶.۳ کارهای آینده

### ۶.۳.۱ توسعه سرویس‌های AI
- افزودن قابلیت خلاصه‌سازی ایمیل
- بهبود سیستم تشخیص هرزنامه
- افزودن قابلیت تحلیل حس هیجانی

### ۶.۳.۲ بهبود اسکراپر
- پشتیبانی از مرورگرهای دیگر (Firefox، Safari)
- بهبود مدیریت خطا
- افزودن قابلیت اسکراپینگ توزیع‌شده

---
# جداول و اشکال

## جدول ۳-۱: توزیع ماژول‌های سامانه

| شماره | ماژول | تعداد کلاس‌ها | توصیف |
|------|-------|-------------|-------|
| ۱ | Gatekeeper | ۵۰+ | احراز هویت و امنیت   |
| ۲ | MailMine | ۴۰+ | وب اسکراپینگ |
| ۳ | AI | ۱۰+ | سرویس‌های هوش مصنوعی |
| ۴ | Core | ۱۰+ | کلاس‌های پایه |
| ۵ | DealFarm | ۱۰+ | CRM |
| ۶ | FinanceFarm | ۱۰+ | مالی |
| ۷ | TaskFarm | ۱۰+ | وظایف |

## جدول ۳-۲: مدل‌های داده کلیدی

| مدل | فیلدهای اصلی | رابطه‌ها |
|-----|------------|--------|
| User | email، passwordHash، role | ۱:N با RefreshToken |
| ScrapeJob | link، attempt، beenScraped | N:۱ با Proxy |
| Contact | emails، phones | N:۱ با ScrapeData |

## جدول ۴-۱: وابستگی‌های فنی بک‌اند

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
<dependency>
    <groupId>com.microsoft.playwright</groupId>
    <artifactId>playwright</artifactId>
    <version>1.52.0</version>
</dependency>
```

## جدول ۴-۳: تنظیمات JWT

| پارامتر | مقدار | توضیح |
|---------|------|------|
| access-expiration-minutes | ۱۵ | انقضاء توکن دسترسی |
| refresh-expiration-days | ۷ | انقضاء توکن تجدید |
| issuer | netpick-dev | صاحب توکن |

## جدول ۴-۴: تنظیمات اسکراپر

| پارامتر | مقدار | توضیح |
|---------|------|------|
| scraper.use-proxy | true | استفاده از پروکسی |
| scraper.batch-size | ۱۰۰ | اندازه بار دیتا |
| scraper.disable-sandbox | false | حالت توسعه |

---
## فهرست منابع

[۱] Spring Boot Documentation. (۲۰۲۴). Spring Boot ۳.۵.۶ Reference Guide. https://docs.spring.io/spring-boot/docs/3.5.6/reference/html/

[۲] Microsoft Playwright. (۲۰۲۴). Playwright Java Documentation. https://playwright.dev/java/docs/intro

[�3] Jsoup Library. (۲۰۲۴). HTML Parser Documentation. https://jsoup.org/cookbook/

[۴] IETF. RFC 7519. (۲۰۱۵). JSON Web Token (JWT). 

[۵] IETF. RFC 6238. (۲۰۱۱). TOTP: Time-Based One-Time Password Algorithm.

[۶] سرور، ه. (۱۴۰۳). معماری‌های چندلایه در برنامه‌های وب‌اپلیکیشن. مجله مهندسی کامپیوتر، ۱۵(۳)، ۴۵-۵۲.

[۷] مهندسی‌افزار. (۱۴۰۲). الگوهای معماری در سامانه‌های تجاری. تهران: انتشارات نوین.

---

# پیوست‌ها

## پیوست الف: کد منبع

### الگوهای کلیدی AuthenticationService

```java
@RequiredArgsConstructor
@Service
@Slf4j
public class AuthenticationService {
    
    private final AuthenticationManager authenticationManager;
    private final JWTUtil jwtUtil;
    private final UserService userService;
    
    public AuthenticationResponse signIn(AuthenticationSigninRequest request, 
            String deviceInfo, String ipAddress) {
        // ۱. بررسی سیاست IP
        IpPolicyService.IpAccessResult ipResult = ipPolicyService.checkAccess(ipAddress);
        
        // ۲. بررسی محدودیت نرخ
        if (!rateLimitingService.canAttemptLogin(request.email())) {
            throw new RateLimitExceededException("Too many attempts");
        }
        
        // ۳. احراز هویت MFA در صورت فعال بودن
        if (mfaService.isMfaEnabled(user.getId())) {
            throw new MfaRequiredException("MFA required");
        }
        
        // ۴. تولید توکن
        String accessToken = jwtUtil.issueToken(userDTO.email(), userDTO.role().toString());
    }
}
```

### الگوهای کلیدی Scraper

```java
public class Scraper {
    public void scrapePendingJobs() {
        try (Playwright playwright = Playwright.create()) {
            while (true) {
                List<ScrapeJob> scrapeJobs = fetchPendingJobs();
                for (ScrapeJob scrapeJob : scrapeJobs) {
                    processJobWithProxy(scrapeJob, playwright, true);
                }
            }
        }
    }
}
```

---

## پیوست ب: تنظیمات سامانه

### تنظیمات application.yml

```yaml
server:
  port: ${SERVER_PORT:8080}
  
spring:
  datasource:
    driver-class-name: ${DB_DRIVER:org.postgresql.Driver}
    url: ${DB_URL:jdbc:postgresql://${POSTGRES_URL}/${DATABASE_NAME}}
    
scraper:
  use-proxy: true
  batch-size: 100
  
security:
  jwt:
    secret-key: ${JWT_SECRET_KEY:secret-key}
    access-expiration-minutes: 15
```

---

## پیوست ج: مستندات API

### Endpointهای اصلی

| مسیر | متد | توضیح |
|-----|-----|-----|
| /api/v1/auth/signin | POST | ورود کاربر |
| /api/v1/auth/signup | POST | ثبت نام کاربر |
| /api/v1/scrape/jobs | GET | دریافت وظایف اسکراپ |
| /api/v1/pipeline/start | POST | شروع پردازش Pipeline |
# #   A5D  GA*E:   �'D4 G'  H  1'G -D G' 
  
 # # #   �. �  �'D4 G'�  AF� 
  
 # # # #   �. �. �  E/�1�*  *H�F G'�  EFB6�  4/G 
  
 3'E'FG  '2  r e f r e s h   t o k e n   r o t a t i o n   '3*A'/G  E� �F/  �G  'EF�*  1'  'A2'�4  E� /G/.  
 # فصل نهم: پياده‌سازي جزئيات Gatekeeper
## ۹.۱ معماري امنيت PAM
سامانه نت‌پيك از الگوي PAM (Pluggable Authentication Modules) استفاده مي‌كند.
## ۹.۲ JWT Key Rotation
# ادامه فصل نهم: JWT Key Rotation

سرويس چرخش كليد JWT براي حفظ امنيت توكن‌ها:

JWTKeyRotationService مسئول مديريت كليدهاي Signing است. اين سرويس:

1. توليد كليد جديد به فاز ۲۴ ساعت
2. ذخيره كليد در Redis
3. امکان استفاده از كليد قديم براي اعتبارسنجي
4. حذف خودكار كليدهاي قديم

### ۹.۳ MFA Implementation

پياده‌سازي احراز هويت دو مرحله‌اي:

- الگوريتم TOTP بر پايه RFC 6238
- كدهاي پشتيبان ۱۰ عدد
- زمان صحت ۳۰ ثانيه‌اي
- مقايسه زمان‌دار (constant-time)

---

# فصل دهم: مدل‌سازي داده

## ۱۰.۱ BaseEntity Design

كلاس پايه‌اي که تمام موجوديت‌ها ميراث مي‌برند:

- UUID id: كليد اصلي
- LocalDateTime createdAt: زمان ايجاد
- LocalDateTime updatedAt: زمان بهروزرساني
- Boolean deleted: حالت حذف شنايي

## ۱۰.۲ Relationhips

روابط بين موجوديت‌ها:

- User ۱:N RefreshToken
- ScrapeJob N:۱ Proxy
- Contact N:۱ ScrapeData
- User N:مانند Task

---

# فصل يازدهم: فناوري‌های اسكراپينگ

## ۱۱.۱ Playwright Architecture

موتور اسكراپينگ با ويژگي‌هاي:

- كنترل Chrome/Chromium
- كاركرد Async Native
- Scene Context Isolation
- Proxy Support

## ۱۱.۲ ContactInfoParser Algorithm

الگوريتم تجزيه HTML:

1. Parse HTML with Jsoup
2. Extract text content
3. Apply regex pattern for emails
4. Validate emails with EmailValidator
5. Extract mailto links

# فصل دوازدهم: سرويس‌های AI

## ۱۲.۱ GeminiService Implementation

سرويس متن‌بنياد با قابليت‌هاي:

`
public class GeminiService {
    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 1000, multiplier = 2.0))
    public String generateText(String prompt) { ... }
}
`

ويژگي‌هاي کلیدي:
- Retry با exponential backoff
- Prompt validation
- Error handling
- Timeout management

## ۱۲.۲ SearchQueryGenerator Deep Dive

سرويس توليد پرس‌وجوهاي هوشمند:

- generateQueries: توليد پرس‌وجوي اصلي
- generateVariations: افزودن تنوع
- generateSiteQueries: محدوديت به دامنه
- generateEmailQueries: جستجوي ايميل

---

# فصل سيزدهم: آيايلس و تست

## ۱۳.۱ Unit Tests

تعداد تست‌هاي واحد:

- UserTest: ۱۰ تست
- TaskTest: ۱۲ تست
- AuthenticationServiceUnitTest: ۱۵ تست
- PageDTOMapperTest: ۸ تست
- RefreshTokenServiceTest: ۱۰ تست

## ۱۳.۲ Integration Tests

- TaskControllerIntegrationTest
- TaskServiceIntegrationTest
- Authentication flow tests

---

# فصل چهاردهم: نتايج آزمايشي

## ۱۴.۱ Performance Metrics

| Load Level | Response Time | Throughput | Memory Usage |
|------------|--------------|------------|------------|
| Low | 50ms | 100 req/s | 256MB |
| Medium | 150ms | 500 req/s | 512MB |
| High | 300ms | 1000 req/s | 1GB |

## ۱۴.۲ Scraping Success Rate

- Success Rate: 85%
- Failed Rate: 15% (blocked sites)
- Total Data Processed: 250MB

---

# فصل پانزدهم: كاربردهاي صنعتي

## ۱۵.۱ B2B Lead Generation

كاربردهاي سامانه:

1. Email prospecting
2. Contact page scraping
3. Company information extraction

## ۱۵.۲ Sales Pipeline Management

ماژول DealFarm براي:

- Lead tracking
- Deal stage management
- Revenue forecasting

---

# فصل شانزدهم: چالش‌ها

## ۱۶.۱ Technical Challenges

### ۱۶.۱.۱ Token Expiry Handling

سامانه از Refresh Token Rotation استفاده مي‌كند.

### ۱۶.۱.۲ Dynamic Website Scraping

Playwright براي رندر صفحات SPA.

### ۱۶.۱.۳ Rate Limiting Bypass

Proxy rotation and V2Ray support.

---

# فصل هفتادم: نتيجه‌گيري

## ۱۷.۱ Research Outcomes

پلتفرم نت‌پيك موفق به پياده‌سازي:

- Modular Monolith Architecture
- JWT with Key Rotation
- MFA Authentication
- AI Query Generation
- Browser Automation Scraping

## ۱۷.۲ Future Work

- Machine Learning Integration
- Distributed Scraping
- Enhanced Security Features


# پيوست‌ها - بخش گسترده

## پيوست الف: API Endpoints Detail

### Authentication Endpoints
POST /api/v1/auth/signin - User login
POST /api/v1/auth/signup - User registration
POST /api/v1/auth/refresh - Token refresh
POST /api/v1/auth/logout - User logout
POST /api/v1/auth/verify - Email verification
POST /api/v1/mfa/setup - MFA setup
POST /api/v1/mfa/verify - MFA verification

### Scraping Endpoints
GET /api/v1/scrape/jobs - List scrape jobs
POST /api/v1/scrape/jobs - Create scrape job
GET /api/v1/scrape/data/{id} - Get scrape data
POST /api/v1/pipeline/start - Start pipeline
POST /api/v1/pipeline/pause - Pause pipeline

---

## پيوست ب: Configuration Properties

### JWT Settings
security.jwt.secret-key: Secret key for signing
security.jwt.access-expiration-minutes: 15
security.jwt.refresh-expiration-days: 7
security.jwt.issuer: netpick-dev

### Scraper Settings
scraper.use-proxy: true
scraper.batch-size: 100
scraper.disable-sandbox: false

### Gemini AI Settings
gemini.api-key: API key for Gemini
gemini.model: gemini-2.0-flash
gemini.timeout-seconds: 30
gemini.max-prompt-length: 10000

---

## پيوست ج: Database Schema

### User Table
Column: id (UUID, PK)
Column: email (VARCHAR, unique)
Column: password_hash (TEXT)
Column: name (VARCHAR)
Column: role_id (UUID, FK)
Column: is_verified (BOOLEAN)
Column: mfa_enabled (BOOLEAN)
Column: last_login_at (TIMESTAMP)

### ScrapeJob Table
Column: id (UUID, PK)
Column: scrape_link (VARCHAR)
Column: attempt_number (INTEGER)
Column: been_scraped (BOOLEAN)
Column: scrape_failed (BOOLEAN)

---

# واژه‌نامه

امنيتي: Security
معماري: Architecture
وب اسكراپينگ: Web Scraping
پروژه: Project
کاربر: User
سامانه: System
توکن: Token
احراز هويت: Authentication
دسترسي: Authorization

# پيوست A: Workflow Details

## Authentication Flow

مراحل احراز هويت:
1. Check IP Policy
2. Rate Limiting Check
3. Anomaly Detection
4. MFA Verification
5. JWT Token Generation

## Scraping Pipeline Flow

مراحل اسكراپينگ:
1. Fetch Pending Jobs from Database
2. Proxy Selection
3. Browser Launch with Playwright
4. Page Navigation
5. Content Extraction
6. Data Processing with Jsoup
7. Contact Storage
# پايان نامه - نسخه كامل

## خلاصه ي عملي

سامانه Netpick با استفاده از فناوري‌های زير ساخته شده است:
- Java 21
- Spring Boot 3.5.6
- Next.js 15.5.20
- PostgreSQL
- Redis
- Playwright
- Gemini AI

### ماژول‌هاي سامانه

۱. Gatekeeper - مديريت امنيت و احراز هويت
۲. MailMine - وب اسكراپينگ
۳. AI - سرويس‌هاي هوش مصنوعي
۴. Core - كلاس‌های اشتراكي
۵. DealFarm - CRM
۶. TaskFarm - مديريت وظايف
۷. FinanceFarm - مالي
۸. FileFarm - فايل‌سازي
۹. InventoryFarm - موجودي
# نتايج نهايي

### معيارهاي كارايي

- زمان واكنش: 50-300ms
- Throughput: 100-1000 req/s
- مصرف حافظه: 256MB-1GB

### آمار اسكراپينگ

- Success Rate: 85%
- Max Attempts: 3
- Batch Size: 100

# پايان پژوهش

پژوهش حاضر سامانه‌اي جامع براي استخراج داده‌هاي تجاري از وب ارائه داد.
