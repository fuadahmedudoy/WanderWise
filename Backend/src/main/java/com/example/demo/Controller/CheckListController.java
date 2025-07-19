package com.example.demo.Controller;

import com.example.demo.Repository.TripActivityRepository;
import com.example.demo.entity.TripActivity;
import com.example.demo.service.TripPlanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/checklist")
public class CheckListController {
    @Autowired
    TripPlanService tripPlanService;
    @Autowired
    TripActivityRepository tripActivityRepository;
    @GetMapping("/{id}")
    public ResponseEntity<?> getActivitiesByTripId(@PathVariable long id ){
        Map<String,Boolean> ans = new HashMap<>();
        if(!tripActivityRepository.existsByTripId(id)){
            ans=tripPlanService.getActivityByTripId(id);
        }
        else{
            List<TripActivity>activity=tripActivityRepository.findAllByTripId(id);

            for(TripActivity s:activity){
                ans.put(s.getActivity(),s.isCompleted());
            }
        }
        return ResponseEntity.ok(ans);
    }
    @PostMapping("/{tripId}/update")
    public ResponseEntity<?> updateChecklist(@PathVariable Long tripId,@RequestBody Map<String,String> request){
        String name=String.valueOf(request.get("activity"));
        String comp=String.valueOf(request.get("completed"));
        boolean flag=true;
        if(!comp.equals("true"))flag=false;
        tripPlanService.updateCheckList(tripId,name,flag);
        return ResponseEntity.ok("Status updated");
    }
}
