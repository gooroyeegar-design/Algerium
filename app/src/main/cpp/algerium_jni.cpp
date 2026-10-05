#include <jni.h>
#include "algerium/engine.h"
#include "algerium/render.h"
#include <sstream>
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

extern "C" JNIEXPORT jstring JNICALL
Java_dz_algerium_browser_NativeEngine_render(JNIEnv* env,jclass,jstring html,jstring url,jfloat width){
 const char* h=env->GetStringUTFChars(html,nullptr); const char* u=env->GetStringUTFChars(url,nullptr);
 auto page=algerium::load_document(h,u,width); auto paint=algerium::paint_document(page.document,page.boxes);
 std::ostringstream out;
 for(const auto& p:paint){
  out<<(int)p.type<<"\t"<<p.x<<"\t"<<p.y<<"\t"<<p.w<<"\t"<<p.h<<"\t"<<p.font_size<<"\t"<<p.color<<"\t";
  for(char ch:p.text){if(ch=='\\')out<<"\\\\";else if(ch=='\t')out<<"\\t";else if(ch=='\n')out<<"\\n";else out<<ch;} out<<"\n";
 }
 env->ReleaseStringUTFChars(html,h); env->ReleaseStringUTFChars(url,u);
 return env->NewStringUTF(out.str().c_str());
}