package com.douban.group;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class HX {
	private static FindImage findImage;
	private static DownloadImage downloadImage;
	private static String targetURL;
	private static List<String> list;
	private static int period;
	private static int total;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		targetURL = "http://www.douban.com/group/haixiuzu/";
		// targetURL = "http://www.douban.com/group/Xsz/";
		// targetURL = "http://www.douban.com/group/441239/";
		// targetURL = "http://www.douban.com/group/fengrufeitun/";
		findImage = new FindImage();
		downloadImage = new DownloadImage();
		list = new ArrayList<String>();
		period = 60000;
		total = 0;
		TimerTask timerTask = new TimerTask() {

			@Override
			public void run() {
				try {
					int hour = Calendar.HOUR_OF_DAY;
					if (hour > 21 || hour < 10) {
						period = 30000;
					} else {
						period = 60000;
					}
					System.out.println(String.format("target url is %s",
							targetURL));
					System.out.println(String.format("period is %ds",
							period / 1000));
					String urlWithName = findImage.find(targetURL);
					if (urlWithName != null) {
						String[] urlwithNameStrings = urlWithName.split(",");
						String theFirstPostURL = urlwithNameStrings[0];
						String theFirstPostName = urlwithNameStrings[1];
						String theFirstPostTitle = urlwithNameStrings[2];
						if (!list.contains(theFirstPostURL)) {
							list.add(theFirstPostURL);
							int downlaodedImages = downloadImage.download(
									theFirstPostURL, theFirstPostName,
									theFirstPostTitle);
							total += downlaodedImages;
							System.err.println(String.format(
									"By now %d images downloaded.", total));
						} else {
							System.out.println("no image found or duplicated.");
						}
					} else {
						System.out.println("no access.");
					}
				} catch (Exception e) {
					System.out.println(e.getMessage());
				}
			}
		};
		Timer timer = new Timer();
		timer.scheduleAtFixedRate(timerTask, 1000, period);
	}
}
