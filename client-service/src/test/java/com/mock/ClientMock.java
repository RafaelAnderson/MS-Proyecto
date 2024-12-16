package com.mock;

import com.ms.client.model.Client;

public class ClientMock {

    public static Client client_personal_vip_dto() {
        return Client.builder()
                .document("12345678")
                .name("Rafael")
                .lastName("Ponte")
                .type(Client.TypeEnum.PERSONAL)
                .profile(Client.ProfileEnum.VIP)
                .build();
    }

    public static Client client_personal_pyme_dto() {
        return Client.builder()
                .document("12345678")
                .name("Rafael")
                .lastName("Ponte")
                .type(Client.TypeEnum.BUSINESS)
                .profile(Client.ProfileEnum.PYME)
                .build();
    }

    public static Client client_personal_vip_entity() {
        return Client.builder()
                .id("h6732hc4f987hg")
                .document("12345678")
                .name("Rafael")
                .lastName("Ponte")
                .address("Lima - Perú")
                .phone("987654321")
                .type(Client.TypeEnum.PERSONAL)
                .profile(Client.ProfileEnum.VIP)
                .build();
    }

    public static Client client_personal_vip_entity_2() {
        return Client.builder()
                .id("h6732hc4f987hg")
                .document("12345678")
                .name("Rafael")
                .lastName("Ponte")
                .address("Lima - Perú")
                .phone("4756787")
                .type(Client.TypeEnum.PERSONAL)
                .profile(Client.ProfileEnum.VIP)
                .build();
    }

    public static Client invalid_profile_personal_pyme() {
        return Client.builder()
                .document("12345678")
                .name("Rafael")
                .lastName("Ponte")
                .type(Client.TypeEnum.PERSONAL)
                .profile(Client.ProfileEnum.PYME)
                .build();
    }

    public static Client invalid_profile_business_vip() {
        return Client.builder()
                .document("12345678")
                .name("Rafael")
                .lastName("Ponte")
                .type(Client.TypeEnum.BUSINESS)
                .profile(Client.ProfileEnum.VIP)
                .build();
    }
}
