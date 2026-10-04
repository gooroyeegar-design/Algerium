#include "algerium/resources.h"
#include "algerium/security.h"
namespace algerium {
bool is_supported_resource_scheme(const std::string& url){auto p=url.find(':');if(p==std::string::npos)return false;auto s=url.substr(0,p);return s=="http"||s=="https"||s=="data"||s=="blob";}
bool should_follow_redirect(const std::string& from,const std::string& to){
 if(!is_safe_navigation(to))return false;
 auto a=parse_origin(from),b=parse_origin(to);
 return !a.opaque&&!b.opaque;
}
}
