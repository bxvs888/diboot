/*
 * Copyright (c) 2015-2020, www.dibo.ltd (service@dibo.ltd).
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * <p>
 * https://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.diboot.core.vo;

import com.diboot.core.binding.annotation.BindEntityList;
import com.diboot.core.entity.Dictionary;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 数据字典的VO，附带子项定义children
 */
@Getter
@Setter
@Accessors(chain = true)
public class DictionaryVO extends Dictionary {
    private static final long serialVersionUID = 7702520653693628994L;

    @BindEntityList(entity= Dictionary.class, condition="this.type=type AND this.id=parent_id", orderBy = "sort_id:ASC", deepBind = true)
    private List<Dictionary> children;

}
