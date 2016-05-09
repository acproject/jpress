/**
 * Copyright (c) 2015-2016, Michael Yang 杨福海 (fuhai999@gmail.com).
 *
 * Licensed under the GNU Lesser General Public License (LGPL) ,Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.jpress.controller.front;

import java.math.BigInteger;

import com.jfinal.plugin.activerecord.Page;

import io.jpress.Consts;
import io.jpress.core.Jpress;
import io.jpress.core.annotation.UrlMapping;
import io.jpress.model.Content;
import io.jpress.model.Taxonomy;
import io.jpress.ui.freemarker.tag.TaxonomyPaginateTag;
import io.jpress.utils.StringUtils;

@UrlMapping(url = Consts.ROUTER_TAXONOMY)
public class TaxonomyController extends BaseFrontController {

	private String moduleName;
	private String slug;
	private int pageNumber;

	public void index() {

		init();

		Taxonomy taxonomy = tryGetTaxonomy();
		if (slug != null && taxonomy == null) {
			renderError(404);
		}

		setAttr(Consts.ATTR_PAGE_NUMBER, pageNumber);
		setAttr("taxonomy", taxonomy);
		setAttr("module", Jpress.currentTemplate().getModuleByName(moduleName));

		BigInteger id = taxonomy == null ? null : taxonomy.getId();
		Page<Content> page = Content.DAO.doPaginate(pageNumber, 10, moduleName, Content.STATUS_NORMAL, id, null, null);
		setAttr("page", page);
		
		TaxonomyPaginateTag tpt = new TaxonomyPaginateTag(page, moduleName, taxonomy);
		setAttr("pagination", tpt);

		if (null == taxonomy) {
			render(String.format("taxonomy_%s.html", moduleName));
		} else {
			render(String.format("taxonomy_%s_%s.html", moduleName, taxonomy.getSlug()));
		}
	}

	private Taxonomy tryGetTaxonomy() {
		return slug == null ? null : Taxonomy.DAO.findBySlugAndModule(slug, moduleName);
	}

	private void init() {
		moduleName = getPara(0);
		if (moduleName == null) {
			renderError(404);
		}

		if (Jpress.currentTemplate().getModuleByName(moduleName) == null) {
			renderError(404);
		}
		
		if(getParaCount() == 1){
			pageNumber = 1;
		}

		else if (getParaCount() == 2) {
			String pageNumberOrSlug = getPara(1);
			if (StringUtils.toInt(pageNumberOrSlug, 0) > 0) {
				pageNumber = StringUtils.toInt(pageNumberOrSlug, 0);
			} else {
				slug = pageNumberOrSlug;
			}
		}
		// 3 para
		else if (getParaCount() >= 3) {
			slug = getPara(1);
			pageNumber = getParaToInt(2);
		}
	}


}
