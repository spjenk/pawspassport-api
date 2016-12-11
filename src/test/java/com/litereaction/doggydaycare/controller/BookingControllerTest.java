package com.litereaction.doggydaycare.controller;

import com.litereaction.doggydaycare.model.Availability;
import com.litereaction.doggydaycare.model.Booking;
import com.litereaction.doggydaycare.model.Pet;
import com.litereaction.doggydaycare.repository.AvailabilityRepository;
import com.litereaction.doggydaycare.repository.BookingRepository;
import com.litereaction.doggydaycare.repository.PetRepository;
import com.litereaction.doggydaycare.util.ModelUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class BookingControllerTest {

    private static final int MAX = 5;
    private static final String BASE_URL = "/bookings/";
    private static final int BOOKING_YEAR = 1999;
    private static final int BOOKING_MONTH = 12;
    private static final int BOOKING_DAY = 31;
    private static final String BOOKING_URL = "/bookings?year=" + BOOKING_YEAR + "&month=" + BOOKING_MONTH + "&day=" + BOOKING_DAY;
    private LocalDate BOOKING_DATE = LocalDate.of(BOOKING_YEAR, BOOKING_MONTH, BOOKING_DAY);

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private PetRepository petRepository;

    @Autowired
    private AvailabilityRepository availabiltyRepository;

    @Before
    public void setup() {
        this.bookingRepository.deleteAllInBatch();
        this.availabiltyRepository.deleteAllInBatch();
        this.petRepository.deleteAllInBatch();
    }

    @After
    public void teardown() {
        this.bookingRepository.deleteAllInBatch();
        this.availabiltyRepository.deleteAllInBatch();
        this.petRepository.deleteAllInBatch();
    }

    @Test
    public void createBookingTest() throws Exception {

        Pet spot = this.petRepository.save(new Pet("Spot", 1));

        Booking booking = new Booking(createAvailability(0), spot);

        ResponseEntity<Booking> response = this.restTemplate.postForEntity(BASE_URL, booking, Booking.class);

        assertThat(response.getStatusCode(), equalTo(HttpStatus.CREATED));
        assertNotNull(response.getBody());

        Booking bookingResult = response.getBody();
        assertThat(bookingResult.getAvailability().getId(), equalTo(ModelUtil.getId(BOOKING_DATE)));
        assertThat(bookingResult.getPet().getId(), equalTo(spot.getId()));
        assertThat(bookingResult.getPet().getName(), equalTo(spot.getName()));

        Availability availability = availabiltyRepository.findOne(ModelUtil.getId(BOOKING_DATE));
        assertThat(availability.getAvailable(), equalTo(4));
    }

    @Test
    public void deleteBookingTest() throws Exception {

        Pet spot = this.petRepository.save(new Pet("Spot", 1));
        Availability availability = availabiltyRepository.save(new Availability(BOOKING_YEAR, BOOKING_MONTH, BOOKING_DAY, MAX));

        Booking booking = new Booking(availability, spot);
        bookingRepository.save(booking);

        this.restTemplate.delete(getUrl(booking));

        ResponseEntity<String> response = this.restTemplate.getForEntity(getUrl(booking), String.class);
        assertThat(response.getStatusCode(), equalTo(HttpStatus.NOT_FOUND));
    }

    @Test
    public void deleteBookingEnsureAvailabilityUpdatesTest() throws Exception {

        Pet spot = this.petRepository.save(new Pet("Spot", 1));
        Availability availability = availabiltyRepository.save(new Availability(BOOKING_YEAR, BOOKING_MONTH, BOOKING_DAY, MAX));

        ResponseEntity<Booking> response =
                this.restTemplate.postForEntity(BASE_URL, new Booking(availability, spot), Booking.class);
        Booking booking = response.getBody();

        availability = availabiltyRepository.findOne(ModelUtil.getId(BOOKING_DATE));
        assertThat(availability.getAvailable(), equalTo(MAX-1));

        this.restTemplate.delete(getUrl(booking));

        availability = availabiltyRepository.findOne(ModelUtil.getId(BOOKING_DATE));
        assertThat(availability.getAvailable(), equalTo(MAX));
    }

    @Test
    public void createBookingNoAvailabilityTest() throws Exception {

        Pet spot = this.petRepository.save(new Pet("Spot", 1));
        Availability availability = availabiltyRepository.save(new Availability(BOOKING_YEAR, BOOKING_MONTH, BOOKING_DAY, 0));

        ResponseEntity<Booking> response =
                this.restTemplate.postForEntity(BASE_URL, new Booking(availability, spot), Booking.class);

        assertThat(response.getStatusCode(), equalTo(HttpStatus.NOT_FOUND));
    }

    private String getUrl(Booking booking) {
        return BASE_URL + booking.getId();
    }

    private Availability createAvailability(int minusDays) {
        return availabiltyRepository.save(new Availability(BOOKING_YEAR, BOOKING_MONTH, BOOKING_DAY-minusDays, MAX));
    }

}
