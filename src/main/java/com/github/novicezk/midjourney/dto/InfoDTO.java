package com.github.novicezk.midjourney.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@ApiModel("Info提交参数")
@EqualsAndHashCode(callSuper = true)
public class InfoDTO extends BaseSubmitDTO {

    @ApiModelProperty(value = "instanceId", required = true, example = "1044074291056099471")
    private String instanceId;
}
