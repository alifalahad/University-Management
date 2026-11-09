@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserRepository userRepository;

    @Test
    void shouldReturnUsers() throws Exception {
        // mock the repository, call mockMvc.perform(get("/users")), 
        // and assert the response
    }
}