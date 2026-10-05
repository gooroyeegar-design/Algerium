#pragma once
#include <string>
#include <vector>
#include <unordered_map>
namespace algerium {
struct CssDeclaration { std::string property,value; };
struct CssRule { std::string selector; std::vector<CssDeclaration> declarations; int specificity=0; };
struct ComputedStyle {
 std::string display="inline",position="static",color="#000000",background="transparent",font_family="sans-serif";
 float font_size=16, margin_top=0,margin_right=0,margin_bottom=0,margin_left=0;
 float padding_top=0,padding_right=0,padding_bottom=0,padding_left=0;
 float width=-1,height=-1;\n float line_height=1.2f,gap=0,border_width=0,border_radius=0;\n std::string flex_direction="row",text_align="left";
 bool bold=false;
};
std::vector<CssRule> parse_css(const std::string& css);
ComputedStyle compute_style(const std::vector<CssRule>& rules,const std::string& tag,const std::unordered_map<std::string,std::string>& attrs,const ComputedStyle* parent=nullptr);
}
