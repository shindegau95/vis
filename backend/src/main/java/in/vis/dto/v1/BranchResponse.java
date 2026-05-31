package in.vis.dto.v1;

import in.vis.model.Branch;

public record BranchResponse(
        Long id,
        String name,
        String city
) {
    public static BranchResponse from(Branch branch) {
        return new BranchResponse(branch.getId(), branch.getName(), branch.getCity());
    }
}
