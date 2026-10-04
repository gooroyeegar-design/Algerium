#pragma once
#include <cstddef>
#include <string>
namespace algerium {
enum class ResourceType { Document, Stylesheet, Script, Image, Font, Media, Other };
struct ResourceRequest { std::string url,referrer; ResourceType type=ResourceType::Other; size_t max_bytes=0; };
bool is_supported_resource_scheme(const std::string& url);
bool should_follow_redirect(const std::string& from,const std::string& to);
}
