package com.litereaction.pawspassport.controller;

import com.litereaction.pawspassport.model.Availability;
import com.litereaction.pawspassport.model.Owner;
import com.litereaction.pawspassport.model.Tenant;
import com.litereaction.pawspassport.repository.AvailabilityRepository;
import com.litereaction.pawspassport.repository.TenantRepository;
import com.litereaction.pawspassport.types.Status;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class TenantControllerTest {

    public static final String TENANTS = "/tenants/";
    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    AvailabilityRepository availabilityRepository;


    @Test
    public void createTentantTest() {
        Tenant tenant = new Tenant("T1");

        ResponseEntity<Tenant> response =
                this.restTemplate.postForEntity(TENANTS, tenant, Tenant.class);

        assertThat(response.getStatusCode() , equalTo(HttpStatus.CREATED));
        assertNotNull(response.getBody());

        Tenant tenantCreated = response.getBody();
        assertThat(tenantCreated.getName(), equalTo("T1"));
        assertThat(tenantCreated.getStatus(), equalTo(Status.ACTIVE));
    }


    @Test

    public void findAllTest() {

        Tenant t1 = tenantRepository.save(new Tenant("T1"));
        Tenant t2 = tenantRepository.save(new Tenant("T2"));

        ResponseEntity<String> response = this.restTemplate.getForEntity(TENANTS, String.class);
        assertNotNull(response.getBody());
        assertThat(response.getBody(), containsString("\"id\":" + t1.getId() + ",\"name\":\"T1\""));
        assertThat(response.getBody(), containsString("\"id\":" + t2.getId() + ",\"name\":\"T2\""));
    }

    @Test
    public void createOwnerTest() throws Exception {

        String ownerName = "Jack";
        String ownerEmail = "abc@edf.com";
        Tenant tenant = this.tenantRepository.save(new Tenant("PnR"));
        Owner owner = new Owner(ownerName, ownerEmail, tenant);

        String url = TENANTS + tenant.getId() + "/owners";
        ResponseEntity<Owner> response =
                this.restTemplate.postForEntity(url, owner, Owner.class);

        assertThat(response.getStatusCode() , equalTo(HttpStatus.CREATED));
        assertNotNull(response.getBody());

        Owner ownerCreated = response.getBody();
        assertThat(ownerCreated.getName(), equalTo(ownerName));
        assertThat(ownerCreated.getEmail(), equalTo(ownerEmail));
    }

    @Test
    public void createOwnerBadRequestTest() throws Exception {

        Tenant tenant = this.tenantRepository.save(new Tenant("PnR"));

        String url = TENANTS + tenant.getId() + "/owners";

        ResponseEntity<Owner> response =
                this.restTemplate.postForEntity(url, new Owner(), Owner.class);

        assertThat(response.getStatusCode() , equalTo(HttpStatus.BAD_REQUEST));

    }

    @Test
    public void findAvailabilityByDateTest() throws Exception {

        Tenant tenant = this.tenantRepository.save(new Tenant("PnR"));
        Availability a1 = this.availabilityRepository.save(new Availability(2016,11,16, 5, tenant));
        Availability a2 = this.availabilityRepository.save(new Availability(2016,11,17, 5, tenant));

        String url = TENANTS + tenant.getId() + "/availability?year=2016&month=11&day=16";

        ResponseEntity<String> response = this.restTemplate.getForEntity(url, String.class);
        assertNotNull(response.getBody());

        assertThat(response.getBody(), containsString(a1.getId()));
        assertThat(response.getBody(), not(containsString(a2.getId())));
    }

    @Test
    public void findAvailabilityWithWrongIdTest() throws Exception {

        String url = "/tenants/9999/availability?year=2016&month=11&day=16";

        ResponseEntity<String> response = this.restTemplate.getForEntity(url, String.class);
        assertThat(response.getStatusCode(), equalTo(HttpStatus.NOT_FOUND));;
    }
}