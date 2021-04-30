# Workout
## Deskripsi Aplikasi
**Fitness App : Workout** adalah aplikasi yang menyediakan berbagai fitur seperti *news*, *training tracker*, *training history*, dan *training scheduler*. 
## Cara Kerja
Ketika pertama membuka aplikasi, *user* akan disambut langsung dengan halaman *news* yang berisikan kumpulan berita-berita olahraga terbaru yang dihimpun dari <https://www.newsapi.org>. Lalu, ketika pengguna memencet salah satu dari berita akan keluar tampilan *web* yang bisa dibaca oleh pengguna. Lalu, ketika *handphone* dimiringkan (*landscape*), maka akan tampil dengan tampilan dua kolom. Lalu, untuk menu/halaman *tracker*, pengguna dapat memilih antara mode bersepeda ataupun mode berlari. Ketika *tracking* selesai, pengguna dapat melihat jarak tempuh untuk mode sepeda dan jumlah langkah untuk mode lari. Di menu utama *tracker* pengguna juga dapat melihat kompas. Lalu, untuk menu/halaman *history*, pengguna dapat melihat sebuah kalender tanpa fungsionalitas (hehe punten kak) Lalu, untuk menu/halaman *scheduler*, ....
## *Library* yang digunakan dan penggunaannya
- **Retrofit**, digunakan untuk *fetch* JSON yang disediakan oleh [API berikut](https://www.newsapi.org).
- **Gson**, digunakan untuk *parsing* JSON.
- **Picasso**, digunakan untuk menampilkan *thumbnail* dari berita.
- **Google Play Service**, digunakan untuk menampilkan peta statik.
## <i>Screenshot</i> Aplikasi
- Tampilan halaman *news* baik *portrait* maupun *landscape*.
<p align="center">
<img src="./images/news-portrait.png" width=200>
<img src="./images/news-landscape.png" height=200>
</p>

- Tampilan *WebView* dari berita yang dipilih.
<p align="center">
<img src="./images/news-web-view.jpg" width=200>
</p>

- Tampilan *Training Tracker*.
<p align="center">
<img src="./images/tracker-main.png" width = 200>
<img src="./images/tracker-info.png" width = 200>
</p>

- Tampilan *Training History*.
<p align="center">
<img src="./images/history-main.png" width = 200>
</p>

## Pembagian Kerja Anggota Kelompok
- 13518092 Izharulhaq:
    - Mengerjakan *Training Scheduler*
- 13518107 Chokyi Ozer:
    - Mengerjakan *Training Tracker*
- 13518149 Stefanus Gusega Gunawan:
    - Mengerjakan *News Fragment*
