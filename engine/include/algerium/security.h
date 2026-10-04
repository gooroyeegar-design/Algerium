#pragma once
#include <string>
namespace algerium {
struct Origin {
 std::string scheme, host, port;
 bool opaque=false;
};
Origin parse_origin(const std::string& url);
bool same_origin(const Origin& a,const Origin& b);
bool is_potentially_trustworthy(const std::string& url);
bool is_safe_navigation(const std::string& url);
bool is_private_host(const std::string& host);
}
