from lxml import html
from sets import Set
import urllib2
import time
import datetime

oldImages=Set()

def downloadImage(title, imageLink, people):
	if '/' in title:
		title = title.replace('/','')
	title=title.encode('utf-8')
	peopleId=people.split('/')[-2]
	now=datetime.datetime.now()
	timestamp='{0}_{1}_{2}_{3}_{4}_{5}'.format(now.year,now.month,now.day,now.hour,now.minute,now.second)
	fileName='{0}_{1}_{2}.jpg'.format(peopleId,timestamp,title)
	file=open(fileName,'wb')
	file.write(urllib2.urlopen(imageLink).read())
	file.close

def getFirstPostImages(title, link, people):
	postHTML=urllib2.urlopen(link)
	postHTMLString=postHTML.read()
	postHTMLTree=html.fromstring(postHTMLString)
	imgTags=postHTMLTree.cssselect('div.topic-content img')
	if len(imgTags) > 0:
		for imgTag in imgTags:
			imageLink=imgTag.attrib['src']
			if imageLink and 'icon' not in imageLink:
				if imageLink not in oldImages:
					downloadImage(title, imageLink, people)
					oldImages.add(imageLink)
				else:
					print "old images found"
	else:
		print 'no images in',link

def getFirstPostInfo():
	targetURL="http://www.douban.com/group/haixiuzu/"
	req=urllib2.Request(targetURL, headers={'User-Agent':'Mozilla/5.0'})
	targetHTMLString=urllib2.urlopen(req).read()
	targetHTMLTree=html.fromstring(targetHTMLString)
	trTags=targetHTMLTree.cssselect('table.olt tr')
	head='class="th"'
	ad="dale_group_special2"
	up="http://img3.douban.com/pics/stick.gif"
	for trTag in trTags:
		trString=html.tostring(trTag)
		if up not in trString and ad not in trString and head not in trString:
			a1Tag=trTag.cssselect('td.title > a')
			a2Tag=trTag.cssselect('td[nowrap="nowrap"] > a')
			title=a1Tag[0].attrib['title']
			link=a1Tag[0].attrib['href']
			people=a2Tag[0].attrib['href']
			break
	getFirstPostImages(title, link, people)

def timer():
	try:
		getFirstPostInfo()
	except:
		pass
	print "downloaded image number is ", len(oldImages)
	now=datetime.datetime.now()
	hour=now.hour
	if hour>21 or hour<9:
		delay=15
	else:
		delay=30
	print delay, " at ", hour, "Clock"
	time.sleep(delay)

while True:
	timer()
