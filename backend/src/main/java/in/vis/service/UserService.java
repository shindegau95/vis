package in.vis.service;

import in.vis.dto.v1.RegisterRequest;
import in.vis.dto.v1.UserResponse;
import in.vis.enums.Role;
import in.vis.exception.NotFoundException;
import in.vis.model.Branch;
import in.vis.model.User;
import in.vis.repository.BranchRepository;
import in.vis.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final BranchRepository branchRepository;

    public UserService(UserRepository userRepository, BranchRepository branchRepository) {
        this.userRepository = userRepository;
        this.branchRepository = branchRepository;
    }

    @Transactional(readOnly = true)
    public UserResponse getByFirebaseUid(String firebaseUid) {
        User user = userRepository.findByFirebaseUid(firebaseUid)
                .orElseThrow(() -> new NotFoundException("User not found: " + firebaseUid));
        return UserResponse.from(user);
    }

    @Transactional
    public UserResponse registerUser(String firebaseUid, RegisterRequest request, Role role, Long branchId) {
        Branch branch = null;
        if (branchId != null) {
            branch = branchRepository.findById(branchId)
                    .orElseThrow(() -> new NotFoundException("Branch not found: " + branchId));
        } else if (role != Role.OWNER) {
            throw new IllegalArgumentException("branchId is required for role " + role);
        }

        User user = new User();
        user.setFirebaseUid(firebaseUid);
        user.setName(request.name());
        user.setEmail(request.email());
        user.setPhone(request.phone());
        user.setRole(role);
        user.setBranch(branch);
        user.setActive(true);

        User saved = userRepository.save(user);
        return UserResponse.from(saved);
    }
}
