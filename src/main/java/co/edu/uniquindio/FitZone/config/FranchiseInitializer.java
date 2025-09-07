package co.edu.uniquindio.FitZone.config;

import co.edu.uniquindio.FitZone.model.entity.Franchise;
import co.edu.uniquindio.FitZone.model.entity.Timeslot;
import co.edu.uniquindio.FitZone.model.enums.DayOfWeek;
import co.edu.uniquindio.FitZone.repository.FranchiseRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Clase encargada de inicializar los datos de la franquicia y sus horarios en la base de datos al iniciar la aplicación.
 */
@Component
public class FranchiseInitializer implements CommandLineRunner {

    private final FranchiseRepository franchiseRepository;

    public FranchiseInitializer(FranchiseRepository franchiseRepository) {
        this.franchiseRepository = franchiseRepository;
    }

    @Override
    public void run(String... args) throws Exception {

        if(franchiseRepository.count() == 0) {

            Franchise franchise = new Franchise();
            franchise.setName("FitZone");

            Set<Timeslot> timeslots = new HashSet<>();

            // Horarios de lunes a viernes

            Timeslot weekdayTimeslot = new Timeslot();
            weekdayTimeslot.setOpenTime(LocalTime.of(5, 0));
            weekdayTimeslot.setCloseTime(LocalTime.of(22, 0));

            Timeslot monday = new Timeslot();
            monday.setDay(DayOfWeek.MONDAY);
            monday.setOpenTime(weekdayTimeslot.getOpenTime());
            monday.setCloseTime(weekdayTimeslot.getCloseTime());
            monday.setFranchise(franchise);
            timeslots.add(monday);

            Timeslot tuesday = new Timeslot();
            tuesday.setDay(DayOfWeek.TUESDAY);
            tuesday.setOpenTime(weekdayTimeslot.getOpenTime());
            tuesday.setCloseTime(weekdayTimeslot.getCloseTime());
            tuesday.setFranchise(franchise);
            timeslots.add(tuesday);

            Timeslot wednesday = new Timeslot();
            wednesday.setDay(DayOfWeek.WEDNESDAY);
            wednesday.setOpenTime(weekdayTimeslot.getOpenTime());
            wednesday.setCloseTime(weekdayTimeslot.getCloseTime());
            wednesday.setFranchise(franchise);
            timeslots.add(wednesday);

            Timeslot thursday = new Timeslot();
            thursday.setDay(DayOfWeek.THURSDAY);
            thursday.setOpenTime(weekdayTimeslot.getOpenTime());
            thursday.setCloseTime(weekdayTimeslot.getCloseTime());
            thursday.setFranchise(franchise);
            timeslots.add(thursday);

            Timeslot friday = new Timeslot();
            friday.setDay(DayOfWeek.FRIDAY);
            friday.setOpenTime(weekdayTimeslot.getOpenTime());
            friday.setCloseTime(weekdayTimeslot.getCloseTime());
            friday.setFranchise(franchise);
            timeslots.add(friday);

            //Horario sábado

            Timeslot saturday = new Timeslot();
            saturday.setDay(DayOfWeek.SATURDAY);
            saturday.setOpenTime(LocalTime.of(8, 0));
            saturday.setCloseTime(LocalTime.of(18, 0));
            saturday.setFranchise(franchise);
            timeslots.add(saturday);

            //Horario domingo

            Timeslot sunday = new Timeslot();
            sunday.setDay(DayOfWeek.SUNDAY);
            sunday.setOpenTime(LocalTime.of(8, 0));
            sunday.setCloseTime(LocalTime.of(13, 0));
            sunday.setFranchise(franchise);
            timeslots.add(sunday);

            franchise.setTimeslots(timeslots);
            franchiseRepository.save(franchise);
        }
    }
}
