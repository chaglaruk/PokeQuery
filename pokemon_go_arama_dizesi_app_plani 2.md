# Pokémon GO Arama Dizesi Oluşturucu — Ürün ve Uygulama Planı

**Hazırlanma tarihi:** 20 Haziran 2026
**Amaç:** Codex'in yazdığı plan promptunu temel alıp, gerçek araştırmayla doğrulanmış, eksikleri tamamlanmış, riskli varsayımları işaretlenmiş tam bir ürün planı.
**Sonraki adım:** Bu dosyayı doğrudan Codex/Antigravity'ye (Gemini 3.1 Pro High) besleyebilirsin. Bölüm 6 ve 19 özellikle kodlama ajanına devredilmek üzere yazıldı.

---

## 0. Codex Promptuna Eleştiri ve Araştırma Bulguları (Önce Bunu Oku)

### Promptta iyi olan kısımlar
- Kapsam tanımı (storage temizliği, takas, IV kontrolü) doğru ve gerçek bir ihtiyaca dayanıyor.
- "Fail-safe by default" ilkesi doğru yaklaşım — bunu güçlendirdim.
- Araştırma gereksinimleri (resmi vs topluluk vs riskli ayrımı) tam isteyeceğim şeydi, onu yaptım.

### Promptta eksik/yanlış olan ve düzelttiğim kısımlar
1. **"Dynamax/Gigantamax/Max-related filters if currently supported"** — bu bir varsayım gibi yazılmıştı. Gerçek: Niantic'in kendi resmi yardım sayfası bunları doğrudan listeliyor (`dynamax`, `gigantamax`, `megaevolve`, `mega1-3`, `fusion`). Şüpheli değil, MVP'ye güvenle eklenebilir.
2. **Operatör çelişkisi tamamen atlanmıştı.** `&` ve `|` ikisi de "VE" demek; "VEYA" için `,`/`;`/`:` kullanılıyor. Bazı popüler rehber siteleri (özellikle reklam/SEO odaklı "AnyTo" gibi spoofing-tool reklamı yapan siteler) `|`'yi yanlışlıkla "VEYA" diye yazıyor. **Uygulama hiçbir zaman `|` üretmemeli**, sadece `&` ve `,` kullanmalı.
3. **`count[N]` tuzağı hiç ele alınmamıştı.** Kopya sayımı dex numarasına göre yapılıyor; shiny, form, gönder/region formu, costume gibi farklılıkları AYIRT ETMİYOR. "Trade fodder" / "duplicate cleanup" özelliği bu yüzden ekstra bir güvenlik katmanı gerektiriyor (Bölüm 7'de detaylı).
4. **Takas (trade) mantığı eksik modellenmişti.** Trade uygunluğu (Standard/Great/Excellent/Special Trade eşiği) arkadaşlık seviyesine bağlıdır — bu Pokémon'un değil, **arkadaşın** özniteliğidir ve hiçbir arama dizesiyle tespit edilemez. Uygulama "takas adayı" derken bunun ne anlama gelip gelmediğini açıkça anlatmalı, aksi halde kullanıcıyı yanıltır.
5. **0% IV / hundo (4★) hassasiyeti yanlış varsayılmıştı.** Yerleşik arama (`0*`...`4*`) IV'yi **bant** halinde filtreler (örn. 4★ = toplam IV 45, yani gerçek hundo; ama 0★ = toplam IV 0-22 aralığı, yani "0% IV" ile **aynı şey değil**, çoğu düşük IV'li Pokémon'u da kapsar). Gerçek 0%/100% kesinliği için CP+HP kombinasyon eşleştirmesi gerekir (pvpivs.com gibi araçların yaptığı şey) — bu MVP için fazla karmaşık, Pro/v1.2'ye bırakılmalı.
6. **Lokalizasyon hiç düşünülmemişti.** Pokémon GO'da Türkçe arayüz dili resmen var (Mart 2022'den itibaren). Ama arama çubuğundaki özel durum kelimelerinin (shiny, legendary, shadow...) Türkçe arayüzde de İngilizce kaldığı mı, yoksa çevrildiği mi **doğrulanamadı** — kaynaklar bunu netleştirmiyor. Bunu varsaymak yerine açıkça "bilinmiyor, test edilmeli" olarak işaretledim ve MVP'yi İngilizce istemciye göre tasarlamayı önerdim.
7. **Etiket (tag) çakışması riski atlanmıştı.** Kullanıcı mevcut bir arama anahtar kelimesiyle aynı isimde özel tag oluşturursa (örn. tag adı "shiny"), o anahtar kelimenin çalışması bozuluyor. Uygulama bunu kontrol etmeli.
8. **"Shortcut phrase" tuzakları atlanmıştı.** `Mega`, `count`, `dynamax`, `gigantamax` gibi kelimeler kısayol olarak otomatik aralığa genişliyor (`Mega` → `Mega0-`), bu da "Meganium" gibi isim aramalarını veya "Dynamax Cannon" gibi hamle aramalarını bozabiliyor.
9. **Plan yapısına eklediğim yeni bölüm:** "Bilgi tabanını güncel tutma" (madde 13) promptta zaten istenmişti ama somut bir mekanizma (versiyonlanmış JSON + son-doğrulama tarihi + topluluk geri bildirim butonu) eklemedim demek olurdu, onu yaptım.

---

## 1. Ürün Vizyonu

Pokémon GO oyuncuları, depolarındaki yüzlerce/binlerce Pokémon'u yönetmek için oyunun güçlü ama keşfedilmesi zor arama çubuğunu kullanmak zorunda; tek bir yazım hatası veya eksik kriter, shiny, legendary ya da yıllarca emek verilmiş bir Pokémon'un yanlışlıkla Profesör'e gönderilmesine yol açabiliyor. Bu uygulama, kullanıcının "ne yapmak istediğini" (güvenli temizlik, 2x aday hazırlığı, takas fodder'ı bulma, 0% IV kontrolü vb.) basit sorularla anlayıp, varsayılan olarak değerli Pokémon'ları koruyan, açıklamalı ve kopyalanabilir bir arama dizesi üreten; çevrimdışı çalışan, hesaba hiç dokunmayan, sade ve hızlı bir "arama dizesi asistanı"dır.

## 2. Hedef Kullanıcılar ve Sorun Noktaları

| Persona | Sorun Noktası |
|---|---|
| **Casual oyuncu** | Arama çubuğunun varlığından bile habersiz veya sözdizimini bilmiyor; depo doluyor, "Pokémon Storage Full" bildirimleri rahatsız ediyor. |
| **Hardcore koleksiyoner / grinder** | Community Day, Spotlight Hour gibi etkinliklerde yüzlerce yakalama yapıyor; her etkinlik sonrası elle temizlik saatler alıyor. |
| **PvP oyuncusu** | Great/Ultra League için doğru IV aralığını bulmak ister ama CP+IV kombinasyon mantığı kafa karıştırıcı. |
| **2x Transfer Candy avcısı** | Bu bonus ~5 haftada bir Spotlight Hour ile geri geliyor (Haziran 2026 itibarıyla doğrulandı); oyuncular bonus öncesi büyük yığınları biriktirip bonus anında güvenle transfer etmek istiyor. |
| **Takas/trade meraklısı** | Hangi kopyaların "güvenle" takas edilebileceğini bilmiyor, yanlışlıkla tek shiny'sini riske atabiliyor. |

**Doğrulanmış gerçek dünya riski:** Reddit'te sıkça paylaşılan "yanlış Pokémon'u transfer ettim" hikayeleri (bazıları şaka gibi anlatılsa da — örneğin bir oyuncunun eşinin kavga sonrası tüm nadir Pokémon'larını silmesi olayı bile topluluğun bu konuya ne kadar duyarlı olduğunu gösteriyor) bu ürünün çözdüğü acının gerçek olduğunu teyit ediyor.

## 3. Rakip / Alternatif Analizi

| Araç | Ne yapıyor | Eksiği |
|---|---|---|
| **Niantic'in dahili "önerilen arama" özelliği** | Oyun içi otomatik öneriler, son aramalar | Çok temel, rehberlik yok, güvenlik uyarısı yok |
| **Pokémon GO Hub Search Strings sayfası** (db.pokemongohub.net/tools/search-strings) | En kapsamlı ve en güncel (Mayıs 2026) referans tablo, Leidwesen'in çalışmasına dayanıyor | Web sayfası, mobil değil; "oluşturucu" yok, sadece referans; rehberlik/güvenlik katmanı yok |
| **pvpivs.com/searchStr.html** | PvP IV'ye özel string oluşturucu, 0% IV dahil/hariç tutma | Sadece IV odaklı, genel storage yönetimi yapmıyor; web tabanlı |
| **pogostring.com / Fly33'in pogo_filter** | Dex numarasına göre takas stringi oluşturma | Çok dar kapsam, shiny/gender/form filtreleme dışında işlevi yok |
| **ark42 / rplus / eski Hub PvE IV builder** | Eski PvE IV string araçları | Leidwesen'in kendi sitesinde bile "büyük ölçüde güncelliğini kaybetti" diye işaretlenmiş |
| **Calcy IV, PokeGenie, Smart IV gibi tarayıcı/IV uygulamaları** | Ekran okuma ile IV tespiti, raid önerisi | Search-string üretimi/güvenli temizlik akışı odaklı değil; bazıları ekran okuma izni istiyor (senin app'inin kaçındığı bir alan) |

**Boşluk:** Hiçbir araç "mobil + çevrimdışı + güvenlik öncelikli + rehberli soru-cevap ile string üreten" bir ürün sunmuyor. Bu, gerçek ve savunulabilir bir niş.

## 4. MVP Özellik Listesi

1. **Hedef seçici ana ekran** — 6-8 net hedef kartı (güvenli temizlik, 2x candy hazırlığı, takas fodder, 0/4★ kontrolü, PvP IV filtresi, etiketsiz/yaşlı yakalamalar).
2. **Rehberli toggle akışı** — her hedef için 2-5 basit evet/hayır veya seçim sorusu.
3. **String üretimi + düz dilde açıklama** — "Bu string ne yapar" her zaman görünür.
4. **Risk rozeti** — her üretilen string için Düşük/Orta/Yüksek risk etiketi + hangi değerli kategorilerin korunduğu/korunmadığı listesi.
5. **Tek dokunuşla kopyalama** + "Oyunda nasıl kullanılır" mini rehber (storage > büyüteç simgesi).
6. **Varsayılan korumalar** (Bölüm 7) — kapatılmadıkça her zaman aktif.
7. **Agresif mod** — açıkça uyarı ekranından geçmeden açılamaz.
8. **Uzman modu** — ham metin editörü + canlı sözdizimi linter (parantez/operatör hatası, rezerve kelime çakışması uyarısı).
9. **Favori şablon kaydetme** (yerel, cihazda).
10. **Bilgi tabanı görüntüleyici** — kategoriye göre filtrelenebilir, "resmi/topluluk/riskli" rozetli referans liste (kullanıcı dilerse kendi stringini de buradan elle kurabilir).
11. **"Bu çalışmadı" geri bildirim butonu** — KB güncelliğini takip etmek için (Bölüm 13).

## 5. MVP Dışına Bırakılacak Özellikler

| Özellik | Neden ertelendi |
|---|---|
| Tam Türkçe arama sözdizimi çevirisi | Lokalizasyon davranışı doğrulanmadı (Bölüm 6.M); yanlış çeviri = kırık string = güven kaybı |
| Tam hassasiyetli 0%/100% IV (CP+HP kombinasyon tablosu) | Her tür için ayrı CP multiplier verisi gerektirir, bakım yükü yüksek; pvpivs.com zaten bunu iyi yapıyor — yarışmak yerine band-tabanlı basit filtreyi MVP'de sun |
| Arkadaş listesi (friends) arama desteği | Farklı bir kullanım senaryosu, ayrı bir ekran ailesi gerektirir |
| Bulut senkronizasyonu / hesap sistemi | "Çevrimdışı-öncelikli, hesaba dokunmama" ilkesiyle gerilim yaratır; v1.2'de opsiyonel yedekleme olabilir |
| Çoklu dil UI (uygulamanın kendi arayüzü) | Türkçe + İngilizce ile başla, gerekirse genişlet |
| Topluluk şablon paylaşım pazarı | Moderasyon yükü + güvenlik riski (kötü niyetli paylaşılan string) MVP'de gereksiz risk |
| Otomatik KB güncelleme (sunucu tarafı scraping) | "No scraping" kısıtına aykırı olabilir; bunun yerine elle doğrulanan versiyonlu JSON (Bölüm 13) |

## 6. Arama Sözdizimi Bilgi Tabanı Yapısı

### Güven Seviyeleri (Tier Tanımı)
- **T1 — Resmi:** Niantic'in kendi yardım merkezi sayfasında (niantic.helpshift.com) doğrudan belgelenmiş.
- **T2 — Topluluk-doğrulanmış:** Resmi sayfada yok ama Pokémon GO Hub / Leidwesen SearchPhrases (db.pokemongohub.net/tools/search-strings, Mayıs 2026 güncel) gibi yıllarca topluluk tarafından test edilmiş, güvenilir, sık güncellenen kaynaklarda detaylı belgelenmiş.
- **T3 — Riskli/Belirsiz:** Kaynaklar birbiriyle çelişiyor veya tek, düşük kaliteli (SEO/reklam amaçlı) bir kaynaktan geliyor. **Uygulama bu kategoriden hiçbir şeyi varsayılan şablonlara koymamalı.**
- **T4 — Lokale bağımlı:** Davranışı oyun dili/istemci ayarına göre değişebilir, doğrulanmamış.

### A) Sayısal / Temel Filtreler (T1)
| Söz Dizimi | Ne işe yarar | Not |
|---|---|---|
| `cp[N]`, `cp[N]-[M]`, `cp[N]-`, `cp-[N]` | CP'ye göre filtre | Aralık, alt/üst sınır hepsi destekleniyor |
| `hp[N]` (+ aralık) | HP'ye göre filtre | **Max HP**'yi kullanır, hasarı görmez |
| `[N]` (sadece sayı) | Pokédex numarası | Ulusal dex numarası |
| `distance[N]` | Yakalama uzaklığı (km) | Tire yoksa "[N]'den az" anlamına gelir (T2 netleştirmesi) |
| `age[N]` | Kaç gün önce yakalandı | **age0** = son 24 saat. Tam 24 saatte bir sıfırlanır. **Takas edilen Pokémon orijinal yakalama tarihini korur, takas tarihini değil** (T2 — önemli ayrıntı, "yaşa göre temizlik" özelliği için kritik) |
| `year[N]`, `year[N]-[M]` | Yakalandığı yıl | 2 haneli yıl = 20xx |

### B) Durum / Özel Etiketler (T1)
| Söz Dizimi | Açıklama |
|---|---|
| `shiny` | Shiny Pokémon |
| `lucky` | Lucky Pokémon |
| `legendary` / `mythical` | Efsanevi / Mitik |
| `shadow` / `purified` | Takım GO Rocket kökenli / arındırılmış |
| `favorite` (veya `favourite`) | Yıldızlanmış favoriler |
| `costume` | Özel etkinlik kostümlü |
| `background` | Nadir arka plan kartı (genel) |
| `locationbackground` | Konum bazlı (yüz yüze etkinlik) arka plan |
| `specialbackground` | Küresel etkinlik arka planı (T2 — resmi sayfada ayrı geçmiyor, Hub tablosunda var, doğrulanmış kabul edilebilir) |
| `traded` | Takasla alınmış |
| `defender` | Şu an spor salonunda |
| `hypertraining` | Hiper antrenmanda |
| `eggsonly` | Sadece yumurtadan çıkan türler |
| `ultrabeast` / `ultra beasts` | Ultra Canavarlar |

### C) IV ve Appraisal (T1 temel kavram + T2 kesin sayılar)
| Söz Dizimi | Açıklama |
|---|---|
| `0*` … `4*` | Toplam IV yıldız bandı. **T2 kesin aralıklar:** 0★=0-22, 1★=23-29, 2★=30-36, 3★=37-44, **4★=45 (gerçek hundo)** |
| `[N]attack`, `[N]defense`, `[N]hp` (0-4) | Tek tek stat appraisal bandı | **T2 kesin aralıklar:** 0=IV 0, 1=IV 1-5, 2=IV 6-10, 3=IV 11-14, 4=IV 15 (mükemmel) |
| ⚠️ **0% IV / 100% IV kesinliği** | Yerleşik arama yalnızca **bant** verir, **tam değer** vermez. 0★ bandı (0-22 toplam) gerçek "0% IV" (0/0/0) ile aynı değildir — çoğu zaman çok daha geniş bir kümedir. Gerçek kesinlik için CP+HP kombinasyon eşlemesi gerekir (Non-MVP, Bölüm 5). |

### D) Form / Seviye Sayaçları (T2 — resmi sayfada yok, Hub'da detaylı)
| Söz Dizimi | Açıklama |
|---|---|
| `buddy[N]` (0-5) | Buddy seviyesi. 0=hiç buddy değil, 1=buddy ama seviye atlamamış, 2-5=İyi/Harika/Ultra/En İyi Buddy |
| `Mega[N]` (0-4) | Mega seviyesi. Yalnız `Mega` yazmak `Mega0-` kısayoludur ⚠️ (bkz. K) |
| `dynamax`, `gigantamax` | Dynamax/Gigantamax türleri — **T1, resmi sayfada da var** |
| `dynamax[N]`, `gigantamax[N]` | Kaç max hamle açılmış (1-3) | Tek başına `dynamax`/`gigantamax` = `[N]1-` kısayolu ⚠️ |
| `candykm[N]` | Buddy şeker mesafesi (1/3/5/20 km) | `candykm20` ile `legendary,mythical,ultrabeast` aynı sonucu verir (eğlenceli ama gerçek bir not) |
| `count[N]` | Kaç kopya var | **⚠️ Dex numarasına göre sayar, shiny/form/gender'ı AYIRT ETMEZ.** Tek başına `count` = `count2-` kısayolu. **Güvenlik riski — Bölüm 7'de detaylı.** |
| `countcandy[N]`, `countcandyxl[N]` | Normal/XL şeker sayısına göre filtre | |
| `party` | Bir "Party" etkinliğinde yakalanmış | Sadece 1 Kasım 2023 sonrası geçerli |

### E) Yakalama Kaynağı / Encounter Type (T2)
| Söz Dizimi | Açıklama |
|---|---|
| `raid`, `remoteraid`, `megaraid`, `exraid`, `primalraid` | Raid türüne göre | 28 Ekim 2020'den öncesini kapsamaz |
| `hatched` | Yumurtadan çıkmış | ~17 Temmuz 2017 sonrası |
| `research` | Araştırma görevlerinden | Alan/Zamanlı/Özel/Koleksiyon görevleri dahil |
| `gbl` | GO Battle League ödülü | |
| `rocket` | Takım GO Rocket savaşından | Raid/research kaynaklı shadow'ları KAPSAMAZ — onlar için `shadow,purified` kullanılmalı |
| `snapshot` | Fotoğraf bombasından | |
| `traded` | Takastan | |
| **Yaban (wild) yakalama** | **Resmi/özel bir kelime YOK.** Workaround: `&!raid&!hatched&!research&!gbl&!rocket&!snapshot&!traded&` |

### F) Evrim (T1/T2 karışık)
| Söz Dizimi | Açıklama |
|---|---|
| `evolve` (T1) | Hemen evrimleşebilir |
| `evolvenew` (T1) | Yeni dex girişi sağlayacak evrim |
| `megaevolve` (T1) | Mega Evrim için yeterli enerji |
| `item` (T1) | Eşya ile evrimleşir |
| `tradeevolve` (T2) | Takasla ücretsiz evrimleşir |
| `evolvequest` (T1) | Görev bazlı evrim |
| `fusion` (T1) | Şu anda sadece **Necrozma ve Kyurem** için geçerli |

### G) Hamle (Move) Araması (T1 temel + T2 detay)
| Söz Dizimi | Açıklama |
|---|---|
| `@[tür]` | O türden hamlesi olanlar (örn. `@grass`) |
| `@[hamle adı]` | Belirli hamle (örn. `@scratch`) |
| `@special` (T1) | Elite TM/legacy/Frustration/Return gerektiren hamleler |
| `@weather` (T1) | Hava durumu bonusu alan hamle |
| `@[1/2/3][kriter]` (T2) | Hamle slotuna göre (1=hızlı, 2=ilk özel, 3=ikinci özel) |
| `!@mov` (T2 — ipucu) | İkinci özel hamlesi açılmış olanları bulma hilesi (placeholder `move_name_0000` mantığından) |
| `adventureeffect` (T2) | **@ önekisi YOK**, kendi başına kullanılır |
| `maxmove[N]`, `maxguard[N]`, `maxspirit[N]` (T2) | Max Battle hamle seviyeleri |

### H) Tür / Bölge / Cinsiyet / Boy (T1)
- **Tipler (18):** grass, water, fire, ground, ice, steel, fairy, electric, flying, poison, ghost, dark, normal, bug, rock, fighting, dragon, psychic
- **Bölgeler (9):** Kanto, Johto, Hoenn, Sinnoh, Unova, Kalos, Galar, Alola, Hisui, Paldea (Galar/Alola/Hisui/Paldea bölgesel formları da getirir)
- **Boy:** `xs`, `xl`, `xxs`, `xxl`
- **Cinsiyet (T2):** `male`, `female`, `genderunknown`
- **`<[tür]` / `>[tür]` (T2):** o türe karşı zayıf / o türe karşı güçlü hamlesi olan

### I) Etiketler (Tags) (T1 + T2)
| Söz Dizimi | Açıklama |
|---|---|
| `#[etiket]` | İsimle etiket arama (otomatik tamamlama) |
| `#` | Tüm etiketlenmiş Pokémon |
| `!#` | Etiketlenmemiş tümü |
| ⚠️ Çakışma riski | Mevcut bir arama kelimesiyle aynı isimde etiket oluşturmak o kelimeyi bozar |

### J) Operatörler (T1 — DOĞRULANMIŞ, ÇELİŞKİ NOTU İLE)
| Operatör | Anlamı | Doğrulama |
|---|---|---|
| `&` | VE (her ikisi de eşleşmeli) | T1, çok kaynakla doğrulandı |
| `,` `;` `:` | VEYA (en az biri eşleşmeli) | T1 |
| `!` | DEĞİL (önüne boşluksuz yazılır) | T1 |
| `\|` | **Pokémon GO Hub/Leidwesen tablosuna göre `&` ile AYNI (VE) anlamına geliyor** | ⚠️ **T3 ÇELİŞKİ:** Bazı düşük kaliteli SEO siteleri `\|`'yi "VEYA" diye tanıtıyor. **Karar: Uygulama hiçbir zaman `\|` üretmemeli, sadece `&` ve `,` kullanmalı.** Belirsizliği kullanıcıya değil, kod tarafına yükle. |
| Sıra | Virgül her zaman `&` bloklarının İÇİNDE değerlendirilir | Karmaşık string'lerde parantez yok — bu yüzden Uzman Modu'nda linter şart |

### K) Bilinen Tuhaflıklar / "Gotcha"lar (T2)
- **Kısayol genişlemesi:** `Mega`→`Mega0-`, `count`→`count2-`, `dynamax`→`dynamax1-`, `gigantamax`→`gigantamax1-`. Bu, "Meganium" adlı Pokémon'u veya "Dynamax Cannon" hamlesini aramayı bozar.
- **Apex Lugia/Ho-Oh workaround:** Özel arama kelimesi yok; `249-250&shadow,purified&research` ile bulunabiliyor.
- **Nickname + sayı tuhaflığı:** Sayıyla başlayan takma adlar bazen sadece sayısal aramada görünmüyor.
- **Noktalama:** "Mr." gibi noktalama içeren isimler, aramada da aynı noktalamayla yazılmalı.

### L) Arkadaş Listesi Aramaları (T2 — MVP dışı ama referans için belgelendi)
`interactable`, `giftable`, `lucky`, `friendlevel[N]` (0-5)

### M) Lokalizasyon / Dil Bağımlılığı (T4 — BELİRSİZ, VARSAYILMADI)
- Pokémon GO'da **Türkçe arayüz dili resmen mevcut** (Mart 2022'den itibaren eklendi).
- **Doğrulanamayan nokta:** Oyun dili Türkçe ayarlıyken arama çubuğundaki özel durum kelimelerinin (`shiny`, `legendary`, `shadow`...), tip adlarının ve bölge adlarının İngilizce mi kaldığı yoksa Türkçeye mi çevrildiği net değil. Niantic'in resmi yardım sayfası farklı dillerde yayınlanıyor ve topluluk bir "Phrase Translator" aracı tutuyor — bu, çevirinin gerçek olduğuna işaret ediyor ama bunu birebir test etmeden MVP'ye koymak risklidir.
- **Karar:** MVP, kullanıcının Pokémon GO istemcisinin **İngilizce** olduğunu varsayar ve bunu UI'da açıkça belirtir ("Bu sürüm İngilizce oyun dili için tasarlanmıştır"). Türkçe istemci desteği, gerçek cihazda elle doğrulanmadan v1.1'e bile girmemeli.

---

## 7. Güvenlik Kuralları ve Fail-Closed Mantığı

### 7.1 Varsayılan Hariç Tutma Listesi (kapatılmadıkça her zaman aktif)
`shiny`, `legendary`, `mythical`, `ultrabeast`, `costume`, `background` (tüm türleri), `4*` (hundo), `0*` (en düşük appraisal — yanlış transfer riski en yüksek kategori çünkü "düşük IV" ile "0% IV" karıştırılabilir), `shadow`, `purified`, `favorite`, `buddy1-` (en az bir kez buddy yapılmış), `lucky`, `traded`, `#` (etiketlenmiş herhangi bir şey), özel formlar (Mega/Dynamax/Gigantamax/Ultra Beast), `xl`/`xxl`/`xs`/`xxs`.

### 7.2 `count[N]` Özel Güvenlik Kuralı (kritik bulgu)
`count` dex numarasına göre sayar; shiny/form'u ayırmaz. Kural:
- Bir şablon `count[N]` kullanıyorsa, **her zaman** ek bir `&!shiny&!lucky&!legendary&!mythical&!shadow&!purified&!favorite&!traded` ekiyle birlikte üretilmeli.
- Açıklama metninde şu uyarı **her zaman** gösterilmeli: *"Bu sayım shiny/form farkını görmez. Aynı türden 1 normal + 1 shiny'n varsa, ikisi de 'kopya' sayılır."*

### 7.3 Takas (Trade) Şeffaflığı
Uygulama, "takas adayı" stringi üretirken şu metni **her zaman** göstermeli: *"Bu liste Pokémon'un kendi özelliklerine göre filtrelenir. Gerçek takas uygunluğu (Standart/Özel Takas) arkadaşının seviyesine bağlıdır ve bu uygulama bunu bilemez."*

### 7.4 Etiket Çakışma Kontrolü
Kullanıcı özel etiket adı girerken, mevcut rezerve kelime listesiyle karşılaştırılır; çakışma varsa uyarı verilir ve `#` öneki otomatik önerilir.

### 7.5 Onay Ekranı Deseni
Her string kopyalanmadan önce: (a) düz dilde ne yaptığı, (b) hangi yüksek-riskli kategorilerin dahil/hariç olduğu, (c) tahmini risk rozeti (Düşük/Orta/Yüksek) gösterilir. Yüksek risk → ikinci bir "Eminim" dokunuşu gerekir.

### 7.6 Tasarım İlkesi: Tek Amaç = Tek (veya 2) String
Operatör önceliği belirsizliği büyütmemek için, bir şablon mümkün olduğunca odaklı tutulur; "her şeyi tek seferde yapan" mega-string'lerden kaçınılır.

### 7.7 Agresif Mod Kuralları
- Ayrı bir ekrandan, en az 1 açık uyarı onayıyla açılır.
- Açıldığında bile `shiny`, `legendary`, `mythical`, `lucky` varsayılan olarak **yine de** hariç kalır — bunlar yalnızca Uzman Modu'nda elle eklenebilir, tek tuşla "agresif mod"la otomatik dahil edilmez.

### 7.8 Uzman Modu
Ham metin editörü + canlı linter: dengesiz operatör, `|` kullanımı (uyarı + `&`'ye dönüştürme önerisi), rezerve kelime + kullanıcı tag çakışması, kısayol-genişleme tuzağı (örn. "Mega" ile başlayan bir isim arıyorsa uyar) kontrolü yapar.

---

## 8. Ekran Bazlı UX Akışı

1. **Ana Ekran** — "Ne yapmak istiyorsun?" kartları (ikon + 1 satır açıklama): Güvenli Temizlik · 2x Şeker Hazırlığı · Takas Fodder · 0★/4★ Kontrolü · PvP IV Filtresi · Eski/Etiketsiz Yakalamalar · Gölge/Arındırılmış Yönetimi · Serbest Oluşturucu (Uzman Modu girişi).
2. **Rehberli Sorular** — seçilen hedefe özel 2-5 toggle/seçim ("Lucky Pokémon'ları her zaman koru mu?" gibi varsayılan AÇIK switch'ler).
3. **Önizleme Ekranı** — üretilen string (büyük, kopyalanabilir kutu) + düz dil açıklaması + risk rozeti + hariç tutulan kategoriler listesi.
4. **Onay/Uyarı Modalı** (yalnızca Orta/Yüksek risk) — "Bu işlem X kategorisini de kapsıyor, devam edilsin mi?"
5. **Oyunda Kullanım Mini-Rehberi** — ilk kullanımda bir kerelik: "Storage > 🔍 simgesi > yapıştır" adımları (gif/illüstrasyon, ekran görüntüsü değil — IP güvenli).
6. **Favoriler** — kaydedilmiş şablonlar listesi, yeniden adlandırma/silme.
7. **Bilgi Tabanı Gezgini** — kategoriye göre filtrelenebilir, tier rozetli, "son doğrulama tarihi" gösteren referans ekran.
8. **Uzman Modu** — ham editör + linter + "Bul" (KB'den terim arama) yan paneli.
9. **Ayarlar** — oyun dili varsayımı, agresif mod açma/kapama, geri bildirim gönderme.

## 8B. Görsel Tasarım Dili (UI)

> Bölüm 8 ekran *akışını* tanımlıyordu; bu bölüm akışın görsel kimliğini tanımlıyor. Sohbette gösterilen 3 ekranlık (Ana Ekran → Soru Ekranı → Önizleme) koyu temalı mockup bu yönergeleri uyguluyor.

- **Tema:** Dark-mode-first, tek tema (açık mod v1.1'e bırakılabilir). Zemin neredeyse siyah-antrasit (#101114), kartlar bir ton açık (#1b1d22), ince 1px düşük-opak beyaz kenarlıklar (rgba(255,255,255,0.06-0.08)) — derin gölge yerine kontrast ile derinlik.
- **Vurgu renkleri:** Her hedef kartı kendi rengiyle ayrışır (teal=temizlik, amber=2x şeker, mercan=takas, mor=IV kontrolü) → kullanıcı hedefleri ikon rengiyle hızlıca tanır. Ana CTA (Kopyala/Önizle) butonu her zaman teal (#1D9E75) — tek bir "birincil eylem rengi" tutarlılığı.
- **Tipografi:** Başlıklar 14px/500, gövde/etiket 12px, ikincil/açıklama metni 11px (minimum okunabilirlik sınırı), kod/string görünümü monospace + teal vurgu.
- **Koruma görselleştirmesi:** Varsayılan olarak korunan kategoriler (shiny, legendary, favori, etiketli...) bir **kilit ikonuyla** gösterilir, normal anahtar/toggle ile değil — bu, "bunlar kolayca kapatılamaz" hissini görsel olarak da verir (Bölüm 7 ile birebir uyumlu).
- **Risk rozeti:** Düşük risk = yumuşak yeşil dolgu + check ikonu; Orta/Yüksek risk için aynı desen amber/kırmızı tonlarına geçer (anlamı renkle taşıyan tek yer — gerisi nötr/marka renginde).
- **Kart tasarımı:** İkon + başlık + tek satır açıklama, köşeleri yuvarlatılmış (14px radius), gölgesiz — düz/sade, hızlı tarama için optimize.
- **Hareket/etkileşim:** Minimal — kopyalama butonunda kısa "Kopyalandı" geri bildirimi dışında animasyon yok; "hızlı ve dikkat dağıtmayan" vizyonla uyumlu.

## 9. Önerilen Uygulama Adları

- **PokeQuery** — kısa, evrensel, App Store aramasında ayırt edici
- **GOBox Builder**
- **Depo Asistanı** (TR pazarı için sade)
- **SafeDex Search**
- **Box Phrase**
- **GO Storage Wizard**

Öneri: Global mağaza için **PokeQuery** veya **SafeDex Search**, alt başlıkta "Search String Builder for Pokémon GO" — Türkçe materyallerde "Depo Asistanı" alt markası kullanılabilir.

## 10. Veri Modeli

```
Term {
  id: String
  syntax: String              // örn. "cp[N]" veya "shiny"
  category: Enum               // Numeric, Status, IV, Counter, EncounterSource, Evolution, Move, Type, Tag, Operator
  tier: Enum<T1, T2, T3, T4>
  description_tr: String
  description_en: String
  riskLevel: Enum<Low, Medium, High>
  sourceUrl: String
  lastVerified: Date
  knownQuirks: String?         // örn. "count kısayolu count2- olarak genişler"
}

Goal {
  id: String
  title: String
  icon: String
  questions: List<ToggleQuestion>
  templateBuilder: (answers) -> GeneratedString
}

ToggleQuestion {
  id: String
  prompt: String
  defaultValue: Boolean
  affectsTerms: List<TermId>
}

GeneratedString {
  rawSyntax: String
  plainLanguageExplanation: String
  excludedCategories: List<TermId>
  includedHighRiskCategories: List<TermId>
  riskLevel: Enum<Low, Medium, High>
}

SavedTemplate {
  id, name, rawSyntax, createdAt, sourceGoalId
}

KnowledgeBaseVersion {
  version: String
  lastUpdated: Date
  termCount: Int
  changelog: String
}
```

## 11. Android-First Teknik Mimari

- **Dil/UI:** Kotlin + Jetpack Compose (mevcut yığınınla uyumlu).
- **Veri katmanı:** Bilgi tabanı, uygulama içine gömülü **versiyonlu JSON asset** olarak gelir (Room gereksiz — veri seti küçük ve nadiren değişiyor). Çalışma zamanında bellekte basit bir liste/map olarak tutulur.
- **Tercih/şablon depolama:** Jetpack **DataStore** (favoriler, ayarlar, son kullanılan hedefler).
- **String motoru:** Saf Kotlin fonksiyonları — `Goal + Answers -> GeneratedString`. Birim test edilebilir, UI'dan tamamen ayrık.
- **Ağ kullanımı:** Çalışma zamanında **opsiyonel** — sadece "KB güncellemesi var mı?" kontrolü için hafif bir GitHub raw JSON fetch (senin mevcut GitHub tabanlı iş akışınla uyumlu). İnternet yoksa uygulama %100 çalışır.
- **DI:** Proje küçük olduğu için Hilt opsiyonel; basit manuel constructor injection yeterli olabilir.
- **Test:** JUnit + Compose UI test; string motoru için "golden file" testleri (Bölüm 14).

## 12. Antigravity İçin Önerilen Stack

- **Ajan:** Gemini 3.1 Pro High — bu proje Antigravity'nin güçlü olduğu "spesifikasyon net, kapsam dar" tipte bir iş, bu yüzden bu plan dosyasının tamamını (özellikle Bölüm 6, 10, 19) ona ilk prompt olarak vermek en verimli yol.
- **Proje yapısı:** Tek modüllü Compose uygulaması; `data/knowledgebase.json`, `domain/StringBuilderEngine.kt`, `ui/screens/*`, `ui/components/*`.
- **Onboarding dosyaları:** Senin mevcut alışkanlığına uygun olarak `AGENTS.md` (ajan kuralları), `CLAUDE.md` (bu projeye özel bağlam — bu plandan türetilmiş kısa özet), `updates.json` (KB versiyon geçmişi).
- **İlk PR kapsamı önerisi:** Sadece MVP'nin 1-2 hedefi (Güvenli Temizlik + 2x Şeker Hazırlığı) + bilgi tabanı motoru; geri kalan hedefler aynı motoru tekrar kullanarak hızlı eklenir.

## 13. Arama Sözdizimini Güncel Tutma Stratejisi

1. Her `Term` kaydında `sourceUrl` ve `lastVerified` alanı zorunlu.
2. **Üç ana kaynak** üç ayda bir elle yeniden kontrol edilir: Niantic resmi yardım sayfası, Pokémon GO Hub/Leidwesen tablosu, ve (varsa) o dönemin güncel Reddit/SilphRoad tartışmaları.
3. Uygulama içi **"Bu çalışmadı" butonu** — kullanıcı bir string'in oyunda beklenmeyen sonuç verdiğini bildirebilir (metin + hangi terim, opsiyonel). Bu geri bildirim sadece geliştiriciye e-posta/issue olarak gider, otomatik hiçbir şey yapmaz (kullanıcı verisi toplanmaz).
4. KB değişiklikleri **changelog** ile versiyonlanır; büyük sözdizimi değişikliklerinde (Niantic bir güncellemeyle terim kaldırırsa/değiştirirse) uygulama güncellemesiyle birlikte KB de güncellenir.
5. **Otomatik scraping YOK** — kullanıcı kısıtına uygun olarak, güncelleme tamamen elle/manuel doğrulama ile yapılır. Bu, "sözdizimi sessizce kırılabilir" riskini tamamen ortadan kaldırmaz (Bölüm 17'de tekrar ele alınıyor) ama ToS'a en uyumlu yoldur.

## 14. Test Planı

### Birim Testleri
- Her `Goal` için: varsayılan cevaplarla üretilen string'in beklenen "golden" çıktıyla eşleştiği testler.
- Aralık biçimlendirme (`cp[N]-[M]`, `cp[N]-`, `cp-[N]`) doğruluğu.
- `!` operatörünün boşluksuz, doğru terimin önüne yerleştirildiği testler.

### Golden Örnekler (örnek küme)
| Hedef | Beklenen string (varsayılan cevaplarla) |
|---|---|
| Güvenli Temizlik | `!shiny&!legendary&!mythical&!ultrabeast&!shadow&!purified&!favorite&!lucky&!#&!traded&0*` (+ açık uyarı: 0★ ≠ kesin 0% IV) |
| 2x Şeker Hazırlığı | `count2-&!shiny&!lucky&!legendary&!mythical&!shadow&!purified&!favorite&!#&!traded` |

### Korkuluk (Guardrail) Testleri
- Varsayılan şablonlar **asla** yüksek riskli kategori içermez (otomatik statik analiz: üretilen string regex ile taranır).
- `count[N]` içeren her şablonda zorunlu ek-dışlama seti var mı kontrolü.
- Üretilen hiçbir string `|` karakteri içermiyor mu kontrolü.
- Kullanıcı tag'i rezerve kelimeyle çakışıyorsa uyarı tetikleniyor mu.

### Gerçek Cihaz Doğrulaması (otomatikleştirilemez — açık not)
Bu uygulama resmi/gayriresmi bir API kullanmadığı için, üretilen string'lerin oyunda gerçekten doğru çalıştığını **otomatik test etmek mümkün değil**. Bu nedenle her KB versiyon güncellemesinden önce, geliştirici elle 10-15 temsili string'i kendi hesabında test etmeli (manuel QA checklist). Bu, ürünün kabul edilen bir kalıcı riskidir (Bölüm 17).

## 15. Güveni Bozmayan Monetizasyon Seçenekleri

- **Temel güvenlik şablonları her zaman ücretsiz** — para kazanma asla "güvenli varsayılanları" kilitlememeli.
- **Tek seferlik "Pro" satın alma** açabileceği şeyler: Uzman Modu'nun gelişmiş linter özellikleri, sınırsız favori şablon, toplu/çoklu string export, gelecekte (doğrulanırsa) CP+HP kombinasyonlu kesin IV modu.
- **Reklam YOK** — hem kullanıcı güveni hem de "sade, hızlı, dikkat dağıtmayan" vizyonla çelişir.
- **Opsiyonel bağış/"kahve ısmarla" butonu** — düşük baskı, güven dostu.

## 16. Play Store Listing Açısı

- **Konumlandırma:** "Yardımcı araç / üretkenlik" kategorisi, oyunun kendisi değil.
- **Ana mesaj:** "Hesabına dokunmadan, internetsiz, Pokémon GO deponu güvenle temizlemenin en kolay yolu."
- **Görsel kimlik:** Pokémon karakterleri/markalı görseller KULLANILMAZ (telif/marka riski); soyut/jenerik "arama/filtre" temalı ikonografi.
- **Açık feragat metni** (Bölüm 17'de tam metin) listing açıklamasında da yer almalı.
- **Anahtar kelimeler:** "pokemon go search strings", "pokemon go storage cleanup", "pokemon go search terms".

## 17. Riskler, Edge Case'ler, Yasal/Disclaimer Wording

### Riskler
- **Sözdizimi sessizce değişebilir** — Niantic önceden haber vermeden terim ekleyebilir/kaldırabilir/davranış değiştirebilir. Otomatik tespit mekanizması yok (Bölüm 14). Azaltma: geri bildirim butonu + üç aylık manuel doğrulama.
- **Lokalizasyon belirsizliği** (Bölüm 6.M) — yanlış varsayımla kırık string üretme riski. Azaltma: MVP'de net "İngilizce istemci" uyarısı.
- **`count` yanlış güveni** — kullanıcı "duplicate" sandığı tek shiny'sini kaybedebilir. Azaltma: Bölüm 7.2 zorunlu uyarı.
- **Kullanıcı Uzman Modu'nda kendi riskli string'ini yazabilir** — uygulama burada sorumluluk alamaz, açık "bu modda korumalar devre dışı" uyarısı gösterilmeli.

### Edge Case'ler
- Aynı Pokémon birden fazla yüksek-riskli kategoriye giriyorsa (örn. shiny + legendary + favorite) — tek bir hariç tutma yeterli, çoğullama gereksiz ama açıklama metninde hepsi listelenmeli.
- Kullanıcının özel tag adı rezerve kelimeyle çakışırsa.
- Apex Lugia/Ho-Oh gibi özel-durum Pokémon'lar için workaround string'ler ayrı, açıkça "deneysel/workaround" etiketli sunulmalı.

### Yasal / Disclaimer Metni (taslak)
> Bu uygulama Niantic, Inc., Nintendo, Game Freak, Creatures Inc. veya The Pokémon Company ile bağlantılı, onaylı veya sponsorlu değildir. Pokémon ve Pokémon GO, ilgili sahiplerinin ticari markalarıdır. Bu uygulama hesabınıza erişmez, oyun verinizi okumaz veya değiştirmez; sadece oyunun kendi arama çubuğunda kullanabileceğiniz metin dizeleri (search string) üretir ve panoya kopyalamanızı sağlar. Üretilen dizelerin doğruluğu Niantic'in oyun güncellemeleriyle değişebilir; lütfen önemli/değerli Pokémon'larınızı transfer etmeden önce sonuçları gözden geçirin.

## 18. Fazlı Yol Haritası

| Faz | Kapsam |
|---|---|
| **MVP** | 4-5 temel hedef (Güvenli Temizlik, 2x Şeker, Takas Fodder, 0★/4★ Kontrolü), varsayılan korumalar, risk rozetleri, favoriler, KB görüntüleyici, geri bildirim butonu |
| **v1.1** | Kalan MVP hedefleri (PvP IV bandı, Gölge/Arındırılmış yönetimi, eski/etiketsiz temizlik), Uzman Modu + linter, agresif mod |
| **v1.2** | Türkçe istemci sözdizimi desteği (**ancak gerçek cihazda doğrulandıktan sonra**), gelişmiş arkadaş listesi entegrasyonu (opsiyonel), çoklu string export |
| **Pro / v2.0** | CP+HP kombinasyonlu kesin 0%/100% IV modu, gelişmiş şablon kütüphanesi, opsiyonel bulut yedekleme |

## 19. Antigravity Build Prompt Outline (Kodlama Ajanı İçin Taslak)

> Bu bölüm, Gemini 3.1 Pro High'a verilecek nihai kodlama promptunun **iskeletidir** — birebir kopyalama değil, senin son rötuşlarınla tamamlanacak bir taslak.

```
ROL: Sen kıdemli bir Android/Kotlin/Jetpack Compose geliştiricisisin.

BAĞLAM: Ekteki "Pokémon GO Arama Dizesi Oluşturucu" ürün planını (Bölüm 1-18) oku.
Bu, [PokemonRarityScanner / TehlikeArşivi] projelerinden TAMAMEN AYRI, yeni ve
bağımsız bir uygulama.

SERT KISITLAR (asla ihlal etme):
- Pokémon GO hesabına giriş YOK.
- Gayriresmi API kullanımı YOK.
- Kullanıcı hesap verisi okuma/scraping YOK.
- Botlama/otomasyon/spoofing özelliği YOK.
- Uygulama SADECE metin (arama dizesi) üretir ve kopyalar.
- Çevrimdışı-öncelikli: internet olmadan tam işlevsel olmalı.

GÖREV 1 (bu oturumda): Bölüm 10'daki veri modelini Kotlin data class'larına çevir.
Bölüm 6'daki bilgi tabanını `knowledgebase.json` olarak yapılandır (her terim:
syntax, category, tier, description, riskLevel, sourceUrl, lastVerified, knownQuirks).

GÖREV 2: Bölüm 7'deki güvenlik kurallarını uygulayan saf Kotlin `StringBuilderEngine`
yaz (UI'dan bağımsız, birim test edilebilir). `count[N]` güvenlik kuralını (7.2) ve
operatör kısıtını (asla `|` üretme, Bölüm 6.J) ZORUNLU mantık olarak gömülü tut.

GÖREV 3: Bölüm 8'deki ekran akışını Compose ile, Bölüm 4'teki ilk 2 MVP hedefiyle
(Güvenli Temizlik + 2x Şeker Hazırlığı) uçtan uca çalışır şekilde kur.

ÇIKTI BEKLENTİSİ: Her görev sonunda, hangi dosyaları oluşturduğunu/değiştirdiğini
ve hangi Bölüm/kuralı nasıl uyguladığını kısaca özetle. Belirsiz bir sözdizimi
kararıyla karşılaşırsan (KB'de "T3 — Riskli" işaretli bir şey), varsayılan
şablonlara EKLEME, sadece Uzman Modu/KB görüntüleyicide referans olarak göster.
```

---

### Kaynak Notu (Şeffaflık İçin)
Bu plandaki sözdizimi bilgileri şu üç kaynağın karşılaştırılmasıyla derlendi: (1) Niantic'in resmi yardım merkezi sayfası (niantic.helpshift.com), (2) Pokémon GO Hub'ın Leidwesen/SearchPhrases temelli, Mayıs 2026'da güncellenmiş arama dizesi tablosu (db.pokemongohub.net/tools/search-strings), (3) pvpivs.com ve çeşitli ikincil topluluk kaynakları (çelişki/doğrulama amaçlı). Tüm "belirsiz" işaretli noktalar bilinçli olarak varsayım yapılmadan bırakılmıştır.
