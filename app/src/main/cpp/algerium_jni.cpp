#include <jni.h>
#include "algerium/engine.h"
#include <string>
static std::string collect(const algerium::Node* n){
 std::string out;
 if(!n)return out;
 if(n->type==algerium::Node::Type::Text)return n->text+" ";
 for(const auto& c:n->children)out+=collect(c.get());
 return out;
}
extern "C" JNIEXPORT jstring JNICALL
Java_dz_algerium_browser_NativeEngine_extractText(JNIEnv* env,jclass,jstring html,jstring url){
 const char* h=env->GetStringUTFChars(html,nullptr);const char* u=env->GetStringUTFChars(url,nullptr);
 auto doc=algerium::parse_html(h,u);std::string text=collect(doc.root.get());
 env->ReleaseStringUTFChars(html,h);env->ReleaseStringUTFChars(url,u);
 return env->NewStringUTF(text.c_str());
}
