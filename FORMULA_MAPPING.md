# Pemetaan formula Excel → Android v4.0

Dokumen ini mencatat formula yang dibaca dari workbook terbaru.

## FASILITAS 2
- B4 = B3/12
- B6 = DATEDIF(B2,TODAY(),"Y")
- B7 = IF(B5<=150000000,1%,1.5%)
- B10 = B5*B7
- B11 = plafond × LOOKUP(rate umur) /10 × ROUNDUP(tenor/12)
- B12 = administrasi bertingkat berdasarkan plafond

## ASN PPPK
- Tenor/umur sama
- Provisi = plafond × 1%
- Premi = formula tabel umur
- Administrasi = tabel plafond

## SERTIFIKASI
- Bunga 11%
- Provisi 1%
- Premi = formula tabel umur
- Administrasi = tabel plafond

## NEW MAP BIRU PRAPENSIUN
- Tenor/umur otomatis
- Flagging = multiplier masa pensiun × 66.600
- Provisi 1% / 1,5% berdasarkan plafond
- Premi = premi asuransi - premi ditanggung nasabah
- Perolehan asuransi = premi + 50% premi ditanggung nasabah
- Administrasi = 1 jt jika tenor <=120, selain itu 1,25 jt
- Total administrasi = administrasi + 865.800 + flagging
- Arus kas = plafond + perolehan asuransi

## NEW MAP BIRU
- Bunga 8,5%
- Provisi 1% / 1,5%
- Premi = premi asuransi - premi nasabah
- Administrasi 1 jt / 1,25 jt berdasarkan tenor <=120
- Arus kas = plafond + premi
- Perolehan asuransi = premi + 50% premi nasabah

## RO MP PRAPEN / RO MAPUT
- Skema 1: nasabah 50%, bank 50%, bunga lama +1%
- Skema 2: nasabah 25%, bank 75%, bunga lama +1,5%
- Provisi 1% / 1,5% berdasarkan plafond
- Administrasi dasar + 865.800 + flagging

## RO PRAPEN
- Bunga <=180 bulan = 7,75%; >180 = 8,25%
- Provisi 1% / 1,5%
- Administrasi dasar + 865.800 + flagging
- Biaya asuransi = premi asuransi - premi ditanggung nasabah

## NEW PENSIUN
- Bunga berdasarkan tier tenor
- Provisi 0,25%
- Premi diperlakukan sebagai input karena sel premi pada workbook bukan formula
- Administrasi = tabel plafond + 666.000

## NEW
- Bunga berdasarkan tier tenor
- Provisi 0,25%
- Premi = formula tabel umur
- Administrasi = tabel plafond
