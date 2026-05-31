package in.vis.dto.v1;

import in.vis.enums.Role;
import in.vis.model.User;

public record UserResponse(
        Long id,
        String firebaseUid,
        String name,
        String email,
        String phone,
        Role role,
        Long branchId,
        String branchName,
        boolean active
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getFirebaseUid(),
                user.getName(),
                user.getEmail(),
                user.getPhone(),
                user.getRole(),
                user.getBranch() != null ? user.getBranch().getId() : null,
                user.getBranch() != null ? user.getBranch().getName() : null,
                user.isActive()
        );
    }
}
