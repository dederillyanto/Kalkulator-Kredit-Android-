package com.jarvis.kalkulatoradminkredit;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.text.NumberFormat;

public class MainActivity extends Activity {
    private static final int BLUE = Color.rgb(11, 61, 145);
    private static final int ACCENT = Color.rgb(11, 110, 243);
    private static final int TEXT = Color.rgb(31, 41, 55);
    private static final int MUTED = Color.rgb(107, 114, 128);
    private static final int BG = Color.rgb(245, 247, 251);

    private ScrollView mainScroll;
    private LinearLayout formContainer;
    private LinearLayout resultContainer;
    private Spinner schemeSpinner;

    private EditText nameField, birthField, tenorField, plafondField, retirementField,
            insurancePremiumField, customerPremiumField, oldInterestField, roSchemeField;
    private LocalDate birthDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(buildScreen());
        setupKeyboardAwareScrolling();
        renderForm(CreditCalculator.Scheme.FASILITAS_2);
    }

    private View buildScreen() {
        mainScroll = new ScrollView(this);
        mainScroll.setFillViewport(true);
        mainScroll.setClipToPadding(false);
        mainScroll.setBackgroundColor(BG);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(18), dp(18), dp(18), dp(28));
        mainScroll.addView(root, new ScrollView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        LinearLayout header = card(BLUE, 18);
        header.setPadding(dp(18), dp(18), dp(18), dp(18));
        TextView title = text("Kalkulator Admin Kredit", 24, Color.WHITE, true);
        TextView sub = text("Perhitungan berdasarkan Excel terbaru • v5.2", 13, Color.rgb(220, 230, 248), false);
        header.addView(title);
        header.addView(space(4));
        header.addView(sub);
        root.addView(header, matchWrap());

        root.addView(space(18));
        root.addView(label("Pilih Skema Kredit"));
        schemeSpinner = new Spinner(this);
        List<String> labels = new ArrayList<>();
        for (CreditCalculator.Scheme s : CreditCalculator.Scheme.values()) labels.add(s.label);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, labels);
        schemeSpinner.setAdapter(adapter);
        schemeSpinner.setBackground(roundRect(Color.WHITE, Color.rgb(210, 215, 225), 12, 1));
        schemeSpinner.setPadding(dp(12), 0, dp(12), 0);
        root.addView(schemeSpinner, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(52)));

        root.addView(space(16));
        formContainer = new LinearLayout(this);
        formContainer.setOrientation(LinearLayout.VERTICAL);
        root.addView(formContainer, matchWrap());

        Button calc = new Button(this);
        calc.setText("HITUNG SEKARANG");
        calc.setTextColor(Color.WHITE);
        calc.setTextSize(15);
        calc.setTypeface(Typeface.DEFAULT_BOLD);
        calc.setAllCaps(false);
        calc.setBackground(roundRect(ACCENT, ACCENT, 14, 0));
        LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(54));
        bp.topMargin = dp(10);
        root.addView(calc, bp);

        resultContainer = new LinearLayout(this);
        resultContainer.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams rp = matchWrap();
        rp.topMargin = dp(18);
        root.addView(resultContainer, rp);

        TextView note = text("Catatan: hasil mengikuti rumus dan konstanta pada file Excel yang menjadi dasar aplikasi. Verifikasi kembali sebelum digunakan untuk transaksi resmi.", 12, MUTED, false);
        note.setPadding(dp(4), dp(12), dp(4), 0);
        root.addView(note, matchWrap());

        TextView watermark = text("DRN", 12, Color.rgb(156, 163, 175), true);
        watermark.setGravity(Gravity.CENTER);
        watermark.setAlpha(0.55f);
        watermark.setLetterSpacing(0.18f);
        watermark.setPadding(0, dp(16), 0, dp(2));
        root.addView(watermark, matchWrap());

        schemeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                renderForm(CreditCalculator.Scheme.values()[position]);
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
        calc.setOnClickListener(v -> calculate());
        return mainScroll;
    }

    private void renderForm(CreditCalculator.Scheme scheme) {
        formContainer.removeAllViews();
        resultContainer.removeAllViews();
        birthDate = null;

        LinearLayout card = card(Color.WHITE, 16);
        card.setPadding(dp(16), dp(16), dp(16), dp(16));
        card.addView(text("Data Perhitungan", 18, TEXT, true));
        card.addView(space(12));

        nameField = addField(card, "Nama Nasabah", "Masukkan nama", InputType.TYPE_CLASS_TEXT);
        birthField = addField(card, "Tanggal Lahir", "Pilih tanggal", InputType.TYPE_CLASS_DATETIME);
        birthField.setFocusable(false);
        birthField.setOnClickListener(v -> showDatePicker());
        tenorField = addField(card, "Tenor (bulan)", "Contoh: 120", InputType.TYPE_CLASS_NUMBER);
        plafondField = addMoneyField(card, "Plafond (Rp)", "Contoh: 150.000.000");

        retirementField = null;
        insurancePremiumField = null;
        customerPremiumField = null;
        oldInterestField = null;
        roSchemeField = null;

        switch (scheme) {
            case MAP_BIRU:
                insurancePremiumField = addMoneyField(card, "Premi Asuransi (Rp)", "Nilai premi asuransi");
                customerPremiumField = addMoneyField(card, "Premi Ditanggung Nasabah (Rp)", "Contoh: 15.000.000");
                break;
            case MAP_BIRU_PRAPENSIUN:
                retirementField = addField(card, "Umur Pensiun Maksimum", "Contoh: 60", InputType.TYPE_CLASS_NUMBER);
                retirementField.setText("60");
                insurancePremiumField = addMoneyField(card, "Premi Asuransi (Rp)", "Nilai premi asuransi");
                customerPremiumField = addMoneyField(card, "Premi Ditanggung Nasabah (Rp)", "Nilai yang ditanggung nasabah");
                break;
            case RO_MP_PRAPEN:
            case RO_MAPUT:
                roSchemeField = addField(card, "Skema RO (1 atau 2)", "1 atau 2", InputType.TYPE_CLASS_NUMBER);
                roSchemeField.setText(scheme == CreditCalculator.Scheme.RO_MAPUT ? "1" : "2");
                retirementField = addField(card, "Umur Pensiun Maksimum", "Contoh: 60", InputType.TYPE_CLASS_NUMBER);
                retirementField.setText("60");
                insurancePremiumField = addMoneyField(card, "Premi Asuransi (Rp)", "Nilai premi asuransi");
                oldInterestField = addField(card, "Bunga Lama (%)", "Contoh: 8,25", InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                break;
            case RO_PRAPEN:
                retirementField = addField(card, "Umur Pensiun Maksimum", "Contoh: 58", InputType.TYPE_CLASS_NUMBER);
                retirementField.setText("58");
                insurancePremiumField = addMoneyField(card, "Premi Asuransi (Rp)", "Nilai premi asuransi");
                customerPremiumField = addMoneyField(card, "Premi Ditanggung Nasabah (Rp)", "Boleh 0");
                break;
            case PENSIUN:
                retirementField = addField(card, "Umur Pensiun Maksimum", "Contoh: 58", InputType.TYPE_CLASS_NUMBER);
                retirementField.setText("58");
                insurancePremiumField = addMoneyField(card, "Premi (Rp)", "Masukkan premi dari sumber perhitungan");
                break;
            case NEW:
                retirementField = addField(card, "Umur Pensiun Maksimum", "Contoh: 58", InputType.TYPE_CLASS_NUMBER);
                retirementField.setText("58");
                break;
            default:
                break;
        }
        formContainer.addView(card, matchWrap());
    }

    private EditText addField(LinearLayout parent, String label, String hint, int inputType) {
        TextView l = text(label, 13, TEXT, true);
        LinearLayout.LayoutParams lp = matchWrap();
        lp.topMargin = dp(7);
        parent.addView(l, lp);

        EditText e = new EditText(this);
        e.setHint(hint);
        e.setTextSize(15);
        e.setTextColor(TEXT);
        e.setHintTextColor(Color.rgb(155, 163, 175));
        e.setSingleLine(true);
        e.setInputType(inputType);
        e.setPadding(dp(12), 0, dp(12), 0);
        e.setBackground(roundRect(Color.rgb(249, 250, 251), Color.rgb(215, 220, 228), 10, 1));
        LinearLayout.LayoutParams ep = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(50));
        ep.topMargin = dp(5);
        ep.bottomMargin = dp(5);
        parent.addView(e, ep);
        e.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) ensureFieldVisible(e);
        });
        e.setOnClickListener(v -> ensureFieldVisible(e));
        return e;
    }

    private EditText addMoneyField(LinearLayout parent, String label, String hint) {
        EditText e = addField(parent, label, hint, InputType.TYPE_CLASS_NUMBER);
        e.addTextChangedListener(new TextWatcher() {
            private boolean editing;
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable editable) {
                if (editing) return;
                String digits = editable.toString().replaceAll("[^0-9]", "");
                if (digits.isEmpty()) return;
                try {
                    editing = true;
                    long value = Long.parseLong(digits);
                    NumberFormat nf = NumberFormat.getIntegerInstance(new Locale("id", "ID"));
                    nf.setGroupingUsed(true);
                    String formatted = nf.format(value);
                    e.setText(formatted);
                    e.setSelection(formatted.length());
                } catch (NumberFormatException ignored) {
                    // Biarkan input tetap tersedia jika angkanya melebihi kapasitas long.
                } finally {
                    editing = false;
                }
            }
        });
        return e;
    }

    private void setupKeyboardAwareScrolling() {
        final View content = mainScroll.getChildAt(0);
        mainScroll.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            Rect visible = new Rect();
            mainScroll.getWindowVisibleDisplayFrame(visible);
            int screenHeight = mainScroll.getRootView().getHeight();
            int keyboardHeight = Math.max(0, screenHeight - visible.bottom);
            boolean keyboardOpen = keyboardHeight > screenHeight * 0.15;

            // Ruang kosong ekstra membuat field terakhir tetap dapat digeser di atas keyboard.
            int bottom = keyboardOpen ? keyboardHeight + dp(32) : dp(28);
            if (content != null && content.getPaddingBottom() != bottom) {
                content.setPadding(content.getPaddingLeft(), content.getPaddingTop(),
                        content.getPaddingRight(), bottom);
            }

            View focused = getCurrentFocus();
            if (keyboardOpen && focused instanceof EditText) {
                mainScroll.postDelayed(() -> ensureFieldVisible(focused), 80);
            }
        });
    }

    private void ensureFieldVisible(View field) {
        if (mainScroll == null || field == null) return;
        mainScroll.postDelayed(() -> {
            Rect rect = new Rect();
            field.getDrawingRect(rect);
            // Tambahkan margin di bawah field agar tidak menempel pada tepi keyboard.
            rect.bottom += dp(24);
            mainScroll.offsetDescendantRectToMyCoords(field, rect);
            int viewportBottom = mainScroll.getHeight() - mainScroll.getPaddingBottom();
            int desiredBottom = viewportBottom - dp(16);
            if (rect.bottom > desiredBottom) {
                mainScroll.smoothScrollBy(0, rect.bottom - desiredBottom);
            } else if (rect.top < dp(16)) {
                mainScroll.smoothScrollBy(0, rect.top - dp(16));
            }
        }, 180);
    }

    private void showDatePicker() {
        LocalDate now = LocalDate.now();
        LocalDate initial = birthDate != null ? birthDate : now.minusYears(30);
        DatePickerDialog dialog = new DatePickerDialog(this, (view, year, month, day) -> {
            birthDate = LocalDate.of(year, month + 1, day);
            birthField.setText(birthDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        }, initial.getYear(), initial.getMonthValue() - 1, initial.getDayOfMonth());
        dialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        dialog.show();
    }

    private void calculate() {
        try {
            CreditCalculator.Scheme scheme = CreditCalculator.Scheme.values()[schemeSpinner.getSelectedItemPosition()];
            CreditCalculator.Input in = new CreditCalculator.Input();
            in.name = textOf(nameField);
            in.birthDate = birthDate;
            in.tenorMonths = intOf(tenorField, "Tenor bulan");
            in.plafond = numberOf(plafondField, "Plafond");
            if (retirementField != null) in.retirementAge = intOf(retirementField, "Umur pensiun maksimum");
            if (insurancePremiumField != null) in.insurancePremium = numberOfAllowZero(insurancePremiumField, "Premi asuransi/premi");
            if (customerPremiumField != null) in.customerPremium = numberOfAllowZero(customerPremiumField, "Premi ditanggung nasabah");
            if (roSchemeField != null) in.roScheme = intOf(roSchemeField, "Skema RO");
            if (oldInterestField != null) in.oldInterestRate = percentInput(oldInterestField, "Bunga lama");

            CreditCalculator.Result result = CreditCalculator.calculate(scheme, in, LocalDate.now());
            showResult(scheme.label, result);
        } catch (Exception ex) {
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void showResult(String scheme, CreditCalculator.Result result) {
        resultContainer.removeAllViews();
        LinearLayout card = card(Color.WHITE, 16);
        card.setPadding(dp(16), dp(16), dp(16), dp(16));
        card.addView(text("Hasil Perhitungan", 19, BLUE, true));
        TextView s = text(scheme, 12, MUTED, false);
        LinearLayout.LayoutParams sp = matchWrap();
        sp.bottomMargin = dp(10);
        card.addView(s, sp);

        for (Map.Entry<String, String> row : result.rows.entrySet()) {
            LinearLayout line = new LinearLayout(this);
            line.setOrientation(LinearLayout.HORIZONTAL);
            line.setGravity(Gravity.CENTER_VERTICAL);
            line.setPadding(0, dp(8), 0, dp(8));

            TextView key = text(row.getKey(), 13, MUTED, false);
            TextView value = text(row.getValue(), 14, TEXT, true);
            value.setGravity(Gravity.END);
            line.addView(key, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
            line.addView(value, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.05f));
            card.addView(line);

            View divider = new View(this);
            divider.setBackgroundColor(Color.rgb(235, 238, 243));
            card.addView(divider, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(1)));
        }
        resultContainer.addView(card, matchWrap());
    }

    private int intOf(EditText field, String label) {
        String s = textOf(field).replace(".", "").replace(",", "");
        if (s.isEmpty()) throw new IllegalArgumentException(label + " wajib diisi.");
        return Integer.parseInt(s);
    }

    private double numberOf(EditText field, String label) {
        double v = numberOfAllowZero(field, label);
        if (v <= 0) throw new IllegalArgumentException(label + " harus lebih dari 0.");
        return v;
    }

    private double numberOfAllowZero(EditText field, String label) {
        String s = textOf(field).replace(" ", "");
        if (s.isEmpty()) throw new IllegalArgumentException(label + " wajib diisi.");
        // Nilai rupiah diasumsikan tanpa desimal. Titik diperlakukan sebagai pemisah ribuan.
        s = s.replace(".", "").replace(",", ".");
        return Double.parseDouble(s);
    }

    private double percentInput(EditText field, String label) {
        String s = textOf(field).replace("%", "").trim().replace(",", ".");
        if (s.isEmpty()) throw new IllegalArgumentException(label + " wajib diisi.");
        return Double.parseDouble(s) / 100.0;
    }

    private String textOf(EditText e) { return e == null ? "" : e.getText().toString().trim(); }
    private int numeric() { return InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL; }

    private LinearLayout card(int color, int radiusDp) {
        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.VERTICAL);
        l.setBackground(roundRect(color, color, radiusDp, 0));
        l.setElevation(dp(2));
        return l;
    }

    private GradientDrawable roundRect(int fill, int stroke, int radiusDp, int strokeDp) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(fill);
        g.setCornerRadius(dp(radiusDp));
        if (strokeDp > 0) g.setStroke(dp(strokeDp), stroke);
        return g;
    }

    private TextView label(String s) {
        TextView v = text(s, 13, TEXT, true);
        LinearLayout.LayoutParams lp = matchWrap();
        lp.bottomMargin = dp(6);
        v.setLayoutParams(lp);
        return v;
    }

    private TextView text(String s, int sp, int color, boolean bold) {
        TextView v = new TextView(this);
        v.setText(s);
        v.setTextSize(sp);
        v.setTextColor(color);
        if (bold) v.setTypeface(Typeface.DEFAULT_BOLD);
        return v;
    }

    private View space(int heightDp) {
        View v = new View(this);
        v.setLayoutParams(new LinearLayout.LayoutParams(1, dp(heightDp)));
        return v;
    }

    private LinearLayout.LayoutParams matchWrap() {
        return new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    private int dp(int value) { return Math.round(value * getResources().getDisplayMetrics().density); }
}
