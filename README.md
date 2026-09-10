# Kalkulator Admin Kredit – Android Source v5.0

Source code Android ini dibuat dari **HITUNGAN_ADMIN_dengan_rumus_premi(4).xlsx**.
Aplikasi menggunakan Java + Android Framework murni (tanpa library UI pihak ketiga) agar proyek sederhana dan mudah dibuka.

## Skema yang tersedia

1. FASILITAS 2
2. ASN PPPK
3. SERTIFIKASI
4. NEW MAP BIRU PRAPENSIUN
5. NEW MAP BIRU
6. RO MP PRAPEN
7. RO MAPUT
8. RO PRAPEN
9. NEW PENSIUN
10. NEW

## Rumus utama yang dipindahkan dari Excel

- Umur: umur tahun penuh dari tanggal lahir sampai tanggal hari ini.
- Tenor tahun: tenor bulan / 12.
- Premi tabel umur: `plafond × rate umur / 10 × ROUNDUP(tenor bulan / 12)`.
- Rate umur: 17–27 = 0,0215; 28–32 = 0,0231; 33–39 = 0,0255; 40–45 = 0,039; 46–50 = 0,0689; 51–55 = 0,0934; 56–58 = 0,1187.
- Administrasi dasar: ≤100 jt = 850 rb; ≤300 jt = 950 rb; ≤500 jt = 1,05 jt; >500 jt = 1,25 jt.
- Provisi dinamis pada skema terkait: ≤150 jt = 1%; >150 jt = 1,5%.
- NEW/NEW PENSIUN bunga tenor: ≤36 = 6%; ≤120 = 7,25%; ≤180 = 7,5%; ≤240 = 7,75%.
- Biaya flagging mengikuti nested IF pada workbook dan dikalikan Rp66.600.

Beberapa nilai premi di workbook adalah nilai input (bukan formula), sehingga aplikasi juga meminta input premi pada skema tersebut.

## Cara termudah membuat APK tanpa Android Studio

1. Buat repository GitHub kosong.
2. Upload **isi folder source ini** ke repository tersebut.
3. Buka tab **Actions**.
4. Pilih **Build Android APK** lalu **Run workflow** (atau cukup push ke branch main/master).
5. Setelah proses selesai, buka hasil workflow dan download artifact **Kalkulator-Admin-Kredit-debug**.
6. Di dalam artifact terdapat `app-debug.apk`.

## Struktur penting

- `app/src/main/java/com/jarvis/kalkulatoradminkredit/CreditCalculator.java` — seluruh mesin perhitungan.
- `app/src/main/java/com/jarvis/kalkulatoradminkredit/MainActivity.java` — tampilan Android.
- `.github/workflows/build-apk.yml` — build APK otomatis via GitHub Actions.

## Catatan validasi

Logika kalkulator sengaja mengikuti formula workbook terbaru. Untuk pemakaian operasional bank, lakukan uji banding dengan contoh data Excel sebelum distribusi resmi.


## Perubahan V5
- Form dapat di-scroll dan menyesuaikan saat keyboard Android tampil.
- Kolom nominal Rupiah memakai pemisah ribuan otomatis (contoh: 150000000 menjadi 150.000.000).


## Perbaikan v5.2
- Auto-scroll field aktif agar tetap terlihat saat keypad/keyboard terbuka.
- Menambahkan ruang scroll dinamis setinggi keyboard untuk field bagian bawah.
- Format ribuan otomatis dan watermark DRN tetap dipertahankan.
