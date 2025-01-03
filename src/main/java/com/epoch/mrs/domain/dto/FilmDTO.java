package com.epoch.mrs.domain.dto;

import com.epoch.mrs.domain.enums.CategoryStatus;
import lombok.Data;
import lombok.experimental.Accessors;



@Data
@Accessors(chain = true)
public class FilmDTO {
    private String title;
    private String info;
    private String description;
    private CategoryStatus category;
}
