package com.jarvis.kalkulatoradminkredit;

import java.time.LocalDate;
import java.time.Period;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Mesin hitung yang menyalin logika dari workbook
 * HITUNGAN_ADMIN_dengan_rumus_premi(4).xlsx.
 *
 * Catatan: beberapa sel premi pada sheet tertentu bukan formula Excel,
 * sehingga diperlakukan sebagai input manual di aplikasi.
 */
public final class CreditCalculator {
    private CreditCalculator() {}

    public enum Scheme {
        FASILITAS_2("FASILITAS 2"),
        ASN_PPPK("ASN PPPK"),
        SERTIFIKASI("SERTIFIKASI"),
        MAP_BIRU_PRAPENSIUN("NEW MAP BIRU PRAPENSIUN"),
        MAP_BIRU("NEW MAP BIRU"),
        RO_MP_PRAPEN("RO MP PRAPEN"),
        RO_MAPUT("RO MAPUT"),
        RO_PRAPEN("RO PRAPEN"),
        PENSIUN("NEW PENSIUN"),
        NEW("NEW");

        public final String label;
        Scheme(String label) { this.label = label; }
    }

    public static final class Input {
        public String name = "";
        public LocalDate birthDate;
        public int tenorMonths;
        public double plafond;
        public int retirementAge;
        public double insurancePremium;
        public double customerPremium;
        public double oldInterestRate;
        public int roScheme;
    }

    public static final class Result {
        public final LinkedHashMap<String, String> rows = new LinkedHashMap<>();
        public void put(String key, String value) { rows.put(key, value); }
    }

    public static Result calculate(Scheme scheme, Input in, LocalDate today) {
        if (in.birthDate == null) throw new IllegalArgumentException("Tanggal lahir wajib diisi.");
        if (in.birthDate.isAfter(today)) throw new IllegalArgumentException("Tanggal lahir tidak boleh melebihi hari ini.");
        if (in.tenorMonths <= 0) throw new IllegalArgumentException("Tenor bulan harus lebih dari 0.");
        if (in.plafond <= 0) throw new IllegalArgumentException("Plafond harus lebih dari 0.");

        int age = Period.between(in.birthDate, today).getYears();
        double tenorYears = in.tenorMonths / 12.0;
        Result r = new Result();
        r.put("Nama", in.name == null || in.name.trim().isEmpty() ? "-" : in.name.trim());
        r.put("Umur", age + " tahun");
        r.put("Tenor", trimNumber(tenorYears) + " tahun (" + in.tenorMonths + " bulan)");
        r.put("Plafond", money(in.plafond));

        switch (scheme) {
            case FASILITAS_2: {
                double provisiRate = dynamicProvisi(in.plafond);
                double premi = standardPremium(in.plafond, age, in.tenorMonths);
                r.put("Bunga", percent(0.10));
                r.put("Persentase Provisi", percent(provisiRate));
                r.put("Provisi", money(in.plafond * provisiRate));
                r.put("Premi", money(premi));
                r.put("Administrasi Kredit", money(baseAdmin(in.plafond)));
                break;
            }
            case ASN_PPPK: {
                double premi = standardPremium(in.plafond, age, in.tenorMonths);
                r.put("Bunga", percent(0.10));
                r.put("Persentase Provisi", percent(0.01));
                r.put("Provisi", money(in.plafond * 0.01));
                r.put("Premi", money(premi));
                r.put("Administrasi Kredit", money(baseAdmin(in.plafond)));
                break;
            }
            case SERTIFIKASI: {
                double premi = standardPremium(in.plafond, age, in.tenorMonths);
                r.put("Bunga", percent(0.11));
                r.put("Persentase Provisi", percent(0.01));
                r.put("Provisi", money(in.plafond * 0.01));
                r.put("Premi", money(premi));
                r.put("Administrasi Kredit", money(baseAdmin(in.plafond)));
                break;
            }
            case MAP_BIRU: {
                requireNonNegative(in.insurancePremium, "Premi asuransi");
                requireNonNegative(in.customerPremium, "Premi ditanggung nasabah");
                double provisiRate = dynamicProvisi(in.plafond);
                double premi = in.insurancePremium - in.customerPremium;
                double admin = in.tenorMonths <= 120 ? 1_000_000 : 1_250_000;
                double cashFlow = in.plafond + premi;
                double insuranceAcquisition = premi + (in.customerPremium * 0.50);
                r.put("Bunga", percent(0.085));
                r.put("Persentase Provisi", percent(provisiRate));
                r.put("Provisi", money(in.plafond * provisiRate));
                r.put("Premi Asuransi", money(in.insurancePremium));
                r.put("Premi Ditanggung Nasabah", money(in.customerPremium));
                r.put("Premi", money(premi));
                r.put("Administrasi Kredit", money(admin));
                r.put("Arus Kas", money(cashFlow));
                r.put("Perolehan Asuransi", money(insuranceAcquisition));
                break;
            }
            case MAP_BIRU_PRAPENSIUN: {
                if (in.retirementAge <= 0) throw new IllegalArgumentException("Umur pensiun maksimum wajib diisi.");
                requireNonNegative(in.insurancePremium, "Premi asuransi");
                requireNonNegative(in.customerPremium, "Premi ditanggung nasabah");
                int remaining = in.retirementAge - age;
                int multiplier = flagMultiplier(remaining, 11);
                double flagging = multiplier * 66_600.0;
                double provisiRate = dynamicProvisi(in.plafond);
                double premi = in.insurancePremium - in.customerPremium;
                double insuranceAcquisition = premi + in.customerPremium * 0.50;
                double admin = in.tenorMonths <= 120 ? 1_000_000 : 1_250_000;
                double totalAdmin = admin + 865_800 + flagging;
                r.put("Umur Pensiun Maksimum", in.retirementAge + " tahun");
                r.put("Sisa Masa Pensiun", remaining + " tahun");
                r.put("Biaya Flagging", money(flagging));
                r.put("Bunga", percent(0.085));
                r.put("Persentase Provisi", percent(provisiRate));
                r.put("Provisi", money(in.plafond * provisiRate));
                r.put("Premi Asuransi", money(in.insurancePremium));
                r.put("Premi Ditanggung Nasabah", money(in.customerPremium));
                r.put("Premi", money(premi));
                r.put("Perolehan Asuransi", money(insuranceAcquisition));
                r.put("Administrasi Kredit", money(admin));
                r.put("Total Administrasi", money(totalAdmin));
                r.put("Arus Kas", money(in.plafond + insuranceAcquisition));
                break;
            }
            case RO_MP_PRAPEN:
            case RO_MAPUT: {
                if (in.retirementAge <= 0) throw new IllegalArgumentException("Umur pensiun maksimum wajib diisi.");
                if (in.roScheme != 1 && in.roScheme != 2) throw new IllegalArgumentException("Skema RO harus 1 atau 2.");
                requireNonNegative(in.insurancePremium, "Premi asuransi");
                if (in.oldInterestRate < 0) throw new IllegalArgumentException("Bunga lama tidak valid.");
                int remaining = in.retirementAge - age;
                int multiplier = flagMultiplier(remaining, 13);
                double flagging = multiplier * 66_600.0;
                double customerShare = in.roScheme == 1 ? 0.50 : 0.25;
                double bankShare = in.roScheme == 1 ? 0.50 : 0.75;
                double extraRate = in.roScheme == 1 ? 0.01 : 0.015;
                double newRate = in.oldInterestRate + extraRate;
                double customerPrem = in.insurancePremium * customerShare;
                double bankPrem = in.insurancePremium * bankShare;
                double provisiRate = dynamicProvisi(in.plafond);
                double admin = baseAdmin(in.plafond);
                r.put("Skema RO", String.valueOf(in.roScheme));
                r.put("Umur Pensiun Maksimum", in.retirementAge + " tahun");
                r.put("Sisa Masa Pensiun", remaining + " tahun");
                r.put("Premi Asuransi", money(in.insurancePremium));
                r.put("Premi Ditanggung Nasabah", money(customerPrem));
                r.put("Premi Ditanggung Bank", money(bankPrem));
                r.put("Biaya Flagging", money(flagging));
                r.put("Bunga Lama", percent(in.oldInterestRate));
                r.put("Bunga Baru", percent(newRate));
                r.put("Persentase Provisi", percent(provisiRate));
                r.put("Provisi", money(in.plafond * provisiRate));
                r.put("Administrasi Kredit", money(admin));
                r.put("Total Administrasi", money(admin + 865_800 + flagging));
                r.put("Biaya Asuransi", money(in.insurancePremium - customerPrem));
                break;
            }
            case RO_PRAPEN: {
                if (in.retirementAge <= 0) throw new IllegalArgumentException("Umur pensiun maksimum wajib diisi.");
                requireNonNegative(in.insurancePremium, "Premi asuransi");
                requireNonNegative(in.customerPremium, "Premi ditanggung nasabah");
                int remaining = in.retirementAge - age;
                int multiplier = flagMultiplier(remaining, 15);
                double flagging = multiplier * 66_600.0;
                double rate = in.tenorMonths <= 180 ? 0.0775 : 0.0825;
                double provisiRate = dynamicProvisi(in.plafond);
                double admin = baseAdmin(in.plafond);
                r.put("Umur Pensiun Maksimum", in.retirementAge + " tahun");
                r.put("Sisa Masa Pensiun", remaining + " tahun");
                r.put("Premi Asuransi", money(in.insurancePremium));
                r.put("Premi Ditanggung Nasabah", money(in.customerPremium));
                r.put("Biaya Flagging", money(flagging));
                r.put("Bunga", percent(rate));
                r.put("Persentase Provisi", percent(provisiRate));
                r.put("Provisi", money(in.plafond * provisiRate));
                r.put("Administrasi Kredit", money(admin));
                r.put("Total Administrasi", money(admin + 865_800 + flagging));
                r.put("Biaya Asuransi", money(in.insurancePremium - in.customerPremium));
                break;
            }
            case PENSIUN: {
                if (in.retirementAge <= 0) throw new IllegalArgumentException("Umur pensiun maksimum wajib diisi.");
                requireNonNegative(in.insurancePremium, "Premi");
                int remaining = in.retirementAge - age;
                int multiplier = flagMultiplier(remaining, 10);
                double flagging = multiplier * 66_600.0;
                double rate = pensionRate(in.tenorMonths);
                if (rate < 0) throw new IllegalArgumentException("Tenor NEW PENSIUN maksimum 240 bulan sesuai Excel.");
                r.put("Umur Pensiun Maksimum", in.retirementAge + " tahun");
                r.put("Sisa Masa Pensiun", remaining + " tahun");
                r.put("Biaya Flagging", money(flagging));
                r.put("Bunga", percent(rate));
                r.put("Persentase Provisi", percent(0.0025));
                r.put("Provisi", money(in.plafond * 0.0025));
                r.put("Premi", money(in.insurancePremium));
                r.put("Administrasi Kredit", money(baseAdmin(in.plafond) + 666_000));
                break;
            }
            case NEW: {
                if (in.retirementAge <= 0) throw new IllegalArgumentException("Umur pensiun maksimum wajib diisi.");
                int remaining = in.retirementAge - age;
                int multiplier = flagMultiplier(remaining, 10);
                double flagging = multiplier * 66_600.0;
                double rate = pensionRate(in.tenorMonths);
                if (rate < 0) throw new IllegalArgumentException("Tenor NEW maksimum 240 bulan sesuai Excel.");
                double premi = standardPremium(in.plafond, age, in.tenorMonths);
                r.put("Umur Pensiun Maksimum", in.retirementAge + " tahun");
                r.put("Sisa Masa Pensiun", remaining + " tahun");
                r.put("Biaya Flagging", money(flagging));
                r.put("Bunga", percent(rate));
                r.put("Persentase Provisi", percent(0.0025));
                r.put("Provisi", money(in.plafond * 0.0025));
                r.put("Premi", money(premi));
                r.put("Administrasi Kredit", money(baseAdmin(in.plafond)));
                break;
            }
        }
        return r;
    }

    public static double standardPremium(double plafond, int age, int tenorMonths) {
        double rate = ageRate(age);
        if (rate < 0) throw new IllegalArgumentException("Umur di luar tabel premi Excel (17–58 tahun).");
        double roundedTenorYears = Math.ceil(tenorMonths / 12.0);
        return plafond * rate / 10.0 * roundedTenorYears;
    }

    public static double ageRate(int age) {
        if (age >= 17 && age <= 27) return 0.0215;
        if (age >= 28 && age <= 32) return 0.0231;
        if (age >= 33 && age <= 39) return 0.0255;
        if (age >= 40 && age <= 45) return 0.0390;
        if (age >= 46 && age <= 50) return 0.0689;
        if (age >= 51 && age <= 55) return 0.0934;
        if (age >= 56 && age <= 58) return 0.1187;
        return -1;
    }

    public static double dynamicProvisi(double plafond) {
        return plafond <= 150_000_000 ? 0.01 : 0.015;
    }

    public static double baseAdmin(double plafond) {
        if (plafond <= 100_000_000) return 850_000;
        if (plafond <= 300_000_000) return 950_000;
        if (plafond <= 500_000_000) return 1_050_000;
        return 1_250_000;
    }

    public static double pensionRate(int tenorMonths) {
        if (tenorMonths <= 36) return 0.06;
        if (tenorMonths <= 120) return 0.0725;
        if (tenorMonths <= 180) return 0.075;
        if (tenorMonths <= 240) return 0.0775;
        return -1;
    }

    /** Mirrors the nested IF pattern in the workbook: <=3 => 0, then +1 per year. */
    public static int flagMultiplier(int remainingYears, int maxCoveredYear) {
        if (remainingYears <= 3) return 0;
        if (remainingYears > maxCoveredYear) return 0;
        return remainingYears - 3;
    }

    private static void requireNonNegative(double value, String label) {
        if (value < 0) throw new IllegalArgumentException(label + " tidak boleh negatif.");
    }

    private static String money(double value) {
        return "Rp " + formatId(Math.round(value));
    }

    private static String percent(double decimal) {
        double p = decimal * 100.0;
        if (Math.abs(p - Math.rint(p)) < 0.0000001) return ((long)Math.rint(p)) + "%";
        return trimNumber(p) + "%";
    }

    private static String trimNumber(double value) {
        if (Math.abs(value - Math.rint(value)) < 0.0000001) return String.valueOf((long)Math.rint(value));
        String s = String.format(java.util.Locale.US, "%.4f", value);
        while (s.contains(".") && (s.endsWith("0") || s.endsWith("."))) {
            s = s.substring(0, s.length() - 1);
        }
        return s.replace('.', ',');
    }

    private static String formatId(long value) {
        String raw = Long.toString(Math.abs(value));
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < raw.length(); i++) {
            if (i > 0 && (raw.length() - i) % 3 == 0) sb.append('.');
            sb.append(raw.charAt(i));
        }
        return value < 0 ? "-" + sb : sb.toString();
    }
}
