package in.vis.service;

import in.vis.dto.v1.RegisterRequest;
import in.vis.dto.v1.UserResponse;
import in.vis.enums.Role;
import in.vis.exception.NotFoundException;
import in.vis.model.Branch;
import in.vis.model.User;
import in.vis.repository.BranchRepository;
import in.vis.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private BranchRepository branchRepository;

    @InjectMocks private UserService userService;

    @Test
    void getByFirebaseUid_existingUser_returnsResponse() {
        Branch branch = new Branch();
        branch.setId(1L);
        branch.setName("Kandivali");

        User user = new User();
        user.setId(10L);
        user.setFirebaseUid("uid-abc");
        user.setName("Aarav");
        user.setEmail("a@b.c");
        user.setRole(Role.CLIENT);
        user.setBranch(branch);
        user.setActive(true);

        when(userRepository.findByFirebaseUid("uid-abc")).thenReturn(Optional.of(user));

        UserResponse response = userService.getByFirebaseUid("uid-abc");

        assertThat(response.firebaseUid()).isEqualTo("uid-abc");
        assertThat(response.name()).isEqualTo("Aarav");
        assertThat(response.role()).isEqualTo(Role.CLIENT);
        assertThat(response.branchId()).isEqualTo(1L);
        assertThat(response.branchName()).isEqualTo("Kandivali");
    }

    @Test
    void getByFirebaseUid_unknownUser_throwsNotFound() {
        when(userRepository.findByFirebaseUid("uid-missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getByFirebaseUid("uid-missing"))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("uid-missing");
    }

    @Test
    void registerUser_persistsAndReturnsResponse() {
        Branch branch = new Branch();
        branch.setId(2L);
        branch.setName("Borivali");
        when(branchRepository.findById(2L)).thenReturn(Optional.of(branch));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(99L);
            return u;
        });

        RegisterRequest req = new RegisterRequest("Priya", "p@x.c", "+91 90000 00000");
        UserResponse response = userService.registerUser("uid-new", req, Role.TRAINER, 2L);

        assertThat(response.id()).isEqualTo(99L);
        assertThat(response.firebaseUid()).isEqualTo("uid-new");
        assertThat(response.name()).isEqualTo("Priya");
        assertThat(response.role()).isEqualTo(Role.TRAINER);
        assertThat(response.branchId()).isEqualTo(2L);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User saved = captor.getValue();
        assertThat(saved.getFirebaseUid()).isEqualTo("uid-new");
        assertThat(saved.getRole()).isEqualTo(Role.TRAINER);
        assertThat(saved.getBranch()).isSameAs(branch);
        assertThat(saved.isActive()).isTrue();
    }
}
