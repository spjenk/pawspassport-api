package com.litereaction.doggydaycare.model;

import com.litereaction.doggydaycare.Model.Booking;
import com.litereaction.doggydaycare.Model.Owner;
import com.litereaction.doggydaycare.Model.Pet;
import org.junit.Assert;
import org.junit.Test;

public class BookingTest {

    @Test
    public void createBookingFromConstructorTest() {

        //when
        String NAME = "Spot";
        String DATE = "19991231";
        Booking booking = new Booking(DATE, new Pet(NAME, 1));

        //then
        Assert.assertEquals(0, booking.getId());
        Assert.assertEquals(DATE, booking.getDate());
        Assert.assertEquals(NAME, booking.getPet().getName());
    }

    @Test
    public void createPawsUsingSettersTest() {

        //given
        Owner owner = new Owner();
        owner.setName("Jack");

        Pet pet = new Pet();
        pet.setId(1);
        pet.setName("Spot");
        pet.setAge(2);
        pet.setOwner(owner);

        //when
        Booking booking = new Booking();
        booking.setId(1);
        booking.setDate("19991231");
        booking.setPet(pet);

        //then
        Assert.assertEquals(1, booking.getId());
        Assert.assertEquals("19991231", booking.getDate());
        Assert.assertEquals("Spot", booking.getPet().getName());
    }
}
