package in.vis.controller;

import in.vis.MediaTypes;
import in.vis.dto.v1.BranchResponse;
import in.vis.exception.NotFoundException;
import in.vis.repository.BranchRepository;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "/branches", produces = {
        MediaTypes.APPLICATION_VND_VIS_V1_JSON_VALUE,
        MediaType.APPLICATION_JSON_VALUE
})
public class BranchController {

    private final BranchRepository branchRepository;

    public BranchController(BranchRepository branchRepository) {
        this.branchRepository = branchRepository;
    }

    @GetMapping
    public List<BranchResponse> list() {
        return branchRepository.findAll().stream()
                .map(BranchResponse::from)
                .toList();
    }

    @GetMapping("/{id}")
    public BranchResponse get(@PathVariable Long id) {
        return branchRepository.findById(id)
                .map(BranchResponse::from)
                .orElseThrow(() -> new NotFoundException("Branch not found: " + id));
    }

    @GetMapping(produces = MediaTypes.APPLICATION_VND_VIS_V2_JSON_VALUE)
    public List<in.vis.dto.v2.BranchResponse> listV2() {
        return branchRepository.findAll().stream()
                .map(in.vis.dto.v2.BranchResponse::from)
                .toList();
    }
}
