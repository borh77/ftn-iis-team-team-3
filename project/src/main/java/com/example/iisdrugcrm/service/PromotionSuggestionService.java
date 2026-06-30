package com.example.iisdrugcrm.service;

import com.example.iisdrugcrm.dto.pricelist.PromotionSuggestionDTO;
import java.util.List;

public interface PromotionSuggestionService {

    List<PromotionSuggestionDTO> getSuggestions(String customerSegment);
}
