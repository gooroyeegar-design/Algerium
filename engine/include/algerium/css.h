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
 float width=-1,height=-1;
 float line_height=1.2f,gap=0,border_width=0,border_radius=0;
 std::string flex_direction="row",text_align="left",overflow="visible",position_type="static";
 float opacity=1, min_width=0,max_width=-1,min_height=0,max_height=-1;
 float flex_grow=0,flex_shrink=1,flex_basis=-1;
 std::string justify_content="flex-start",align_items="stretch";
 float letter_spacing=0,word_spacing=0;
 bool bold=false;
};
std::vector<CssRule> parse_css(const std::string& css);
ComputedStyle compute_style(const std::vector<CssRule>& rules,const std::string& tag,const std::unordered_map<std::string,std::string>& attrs,const ComputedStyle* parent=nullptr);
}
