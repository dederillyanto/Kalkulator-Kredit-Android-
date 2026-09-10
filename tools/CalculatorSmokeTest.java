import com.jarvis.kalkulatoradminkredit.CreditCalculator;
import java.time.LocalDate;

public class CalculatorSmokeTest {
    public static void main(String[] args) {
        CreditCalculator.Input in = new CreditCalculator.Input();
        in.name = "TEST";
        in.birthDate = LocalDate.of(1980, 6, 1);
        in.tenorMonths = 72;
        in.plafond = 26_000_000;
        CreditCalculator.Result r = CreditCalculator.calculate(
                CreditCalculator.Scheme.FASILITAS_2, in, LocalDate.of(2026, 9, 10));
        if (!r.rows.containsKey("Premi")) throw new AssertionError("Premi missing");
        System.out.println(r.rows);
    }
}
