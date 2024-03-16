package com.iyzipay.demo.model.sample;

import com.iyzipay.model.Address;

public class SampleAdress {
    public SampleAdress() {
    }

    public Address getSampleAddress(){
        Address billingAddress = new Address();
        billingAddress.setContactName("Jane Doe");
        billingAddress.setCity("Istanbul");
        billingAddress.setCountry("Turkey");
        billingAddress.setAddress("Nidakule Göztepe, Merdivenköy Mah. Bora Sok. No:1");
        billingAddress.setZipCode("34742");

        return billingAddress;
    }
}
