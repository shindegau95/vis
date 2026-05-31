package in.vis.dto.v2;

import in.vis.model.Branch;

public record BranchResponse(
        Long id,
        String name,
        String city,
        String slug
) {
    public static BranchResponse from(Branch branch) {
        return new BranchResponse(
                branch.getId(),
                branch.getName(),
                branch.getCity(),
                slugify(branch.getName()));
    }

    private static String slugify(String name) {
        return name == null ? null : name.toLowerCase().replace(' ', '-');
    }
}
