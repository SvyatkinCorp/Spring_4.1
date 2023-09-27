package com.itm.space.backendresources;

import com.itm.space.backendresources.api.request.UserRequest;
import com.itm.space.backendresources.api.response.UserResponse;
import com.itm.space.backendresources.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.RoleMappingResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.MappingsRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;

import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WithMockUser(username = "Sergei", password = "1234", authorities = "ROLE_MODERATOR")
public class UserControllerTest extends BaseIntegrationTest {

    private final UserService userService;

    @Autowired
    public UserControllerTest(UserService userService) {
        this.userService = userService;
    }

    @MockBean
    private Keycloak keycloak;

    @MockBean
    private RealmResource realmResource;

    @MockBean
    private UsersResource usersResource;


    @MockBean
    private RoleMappingResource roleMappingResource;

    @MockBean
    private MappingsRepresentation mappingsRepresentation;


    @Test
    void shouldGetHelloPage() throws Exception {
        mvc.perform(get("/api/users/hello"))
                .andExpect(status().isOk())
                .andExpect(content().string("Sergei"));
    }

    @BeforeEach
    public void setup() {
        when(keycloak.realm(anyString())).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
    }

    @Test
    void createControllerTest() throws Exception {

        UserRequest userRequest = new UserRequest("Vasya", "1234@mail.com", "12345", "Vasya", "Svyatkin");
        Response response = Response.status(Response.Status.CREATED).location(new URI("user_id")).build();
        when(usersResource.create(any())).thenReturn(response);


        mvc.perform(requestWithContent(post("/api/users"), userRequest));
        verify(usersResource, times(1)).create(any());
    }

    @Test
    void ControllerUserById() throws Exception {

        UserRepresentation userRepresentation = new UserRepresentation();
        UUID userId = UUID.randomUUID();
        userRepresentation.setId(String.valueOf(userId));
        userRepresentation.setFirstName("Petya");

        when(usersResource.get(anyString())).thenReturn(mock(UserResource.class));
        when(keycloak.realm(anyString()).users().get(anyString()).toRepresentation()).thenReturn(userRepresentation);
        when(keycloak.realm(anyString()).users().get(anyString()).roles()).thenReturn(roleMappingResource);
        when(keycloak.realm(anyString()).users().get(anyString()).roles().getAll()).thenReturn(mappingsRepresentation);


        UserResponse response = userService.getUserById(userId);


        mvc.perform(requestWithContent(get("/api/users/{id}", userId), response));
        assertEquals("Petya", response.getFirstName());


    }
}