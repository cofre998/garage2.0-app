import java.util.Comparator;
import com.ger.garage.model.Booking;

public class BookingIdComparator implements Comparator<Booking> {

    @Override
    public int compare(Booking b1, Booking b2) {

        // 🔥 ORDENAR POR FECHA (RECOMENDADO)
        if (b1.getDate() != null && b2.getDate() != null) {
            return b2.getDate().compareTo(b1.getDate());
        }

        // 🔥 FALLBACK POR ID STRING
        return b2.getId().compareTo(b1.getId());
    }
}