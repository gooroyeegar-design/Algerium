#pragma once
#include "dom.h"
#include "css.h"
#include "layout.h"
namespace algerium {
Document parse_html(const std::string& html,const std::string& url);
struct Page { Document document; std::vector<CssRule> rules; std::vector<LayoutBox> boxes; };
Page load_document(const std::string& html,const std::string& url,float viewport_width);
}