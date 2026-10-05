#pragma once
#include <string>
#include <vector>
namespace algerium {
struct Cookie { std::string name,value,domain,path; bool secure=false,http_only=false; long long expires_at=-1; };
class CookieJar {
 public:
  bool set_cookie(const std::string&header,const std::string&request_url,long long now_seconds);
  std::string cookie_header(const std::string&request_url,long long now_seconds) const;
  void clear();
 private: std::vector<Cookie> cookies_;
};
}